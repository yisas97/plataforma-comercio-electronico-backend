package pe.com.prueba.plataformacontrolcomercio.service.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.dto.CreateOrderItemRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.CreateOrderRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderStatsDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.producer.ProducerSalesStatsDTO;
import pe.com.prueba.plataformacontrolcomercio.mapper.OrderMapper;
import pe.com.prueba.plataformacontrolcomercio.model.Order;
import pe.com.prueba.plataformacontrolcomercio.model.OrderItem;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.User;
import pe.com.prueba.plataformacontrolcomercio.repository.CartItemRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.OrderRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProductRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartItemRepository cartItemRepository,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest createOrderRequest) {
        log.info("Creating order for user: {}", userId);

        // Validar que el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Crear la orden
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(createOrderRequest.getShippingAddress());
        order.setPaymentMethod(createOrderRequest.getPaymentMethod());
        order.setNotes(createOrderRequest.getNotes());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setOrderItems(new ArrayList<>());

        // Procesar items del pedido
        double totalAmount = 0.0;

        for (CreateOrderItemRequest itemRequest : createOrderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemRequest.getProductId()));

            // Validar stock
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + product.getName() +
                        ". Stock disponible: " + product.getQuantity());
            }

            // Crear item de la orden
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice()); // Precio al momento de la compra

            order.getOrderItems().add(orderItem);
            totalAmount += orderItem.getSubtotal();

            // Reducir stock del producto
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        // Guardar la orden
        Order savedOrder = orderRepository.save(order);

        // Limpiar carrito del usuario
        cartItemRepository.deleteByUserId(userId);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.info("Getting orders for user: {}", userId);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderDTO> getOrderByIdAndUserId(Long orderId, Long userId) {
        log.info("Getting order {} for user {}", orderId, userId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Verificar que la orden pertenece al usuario
            if (order.getUser().getId().equals(userId)) {
                return Optional.of(orderMapper.toDTO(order));
            }
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<OrderDTO> cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order {} for user {}", orderId, userId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Verificar que la orden pertenece al usuario
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Esta orden no pertenece al usuario");
        }

        // Solo se puede cancelar si está en estado PENDING o CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("No se puede cancelar una orden en estado: " + order.getStatus());
        }

        // Restaurar stock de los productos
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setQuantity(product.getQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        }

        // Cambiar estado a cancelado
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return Optional.of(orderMapper.toDTO(updatedOrder));
    }

    @Override
    public OrderStatsDTO getOrderStatsByUserId(Long userId) {
        log.info("Getting order stats for user: {}", userId);

        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long totalOrders = userOrders.size();
        long pendingOrders = userOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.PENDING ? 1 : 0)
                .sum();
        long completedOrders = userOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.DELIVERED ? 1 : 0)
                .sum();
        long cancelledOrders = userOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.CANCELLED ? 1 : 0)
                .sum();

        double totalSpent = userOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        return new OrderStatsDTO(totalOrders, pendingOrders, completedOrders, cancelledOrders, totalSpent);
    }

    @Override
    public List<OrderDTO> getOrdersByProducerId(Long producerId) {
        log.info("Getting orders for producer: {}", producerId);

        List<Order> orders = orderRepository.findByProducerIdOrderByCreatedAtDesc(producerId);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<OrderDTO> updateOrderStatus(Long orderId, OrderStatus status, Long producerId) {
        log.info("Updating order {} status to {} by producer {}", orderId, status, producerId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Verificar que al menos uno de los productos de la orden pertenece al productor
        boolean hasProducerProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getProducer().getId().equals(producerId));

        if (!hasProducerProduct) {
            throw new IllegalArgumentException("Esta orden no contiene productos del productor especificado");
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return Optional.of(orderMapper.toDTO(updatedOrder));
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        log.info("Getting all orders for admin");

        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Getting orders by status: {}", status);

        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByProducerIdAndStatus(Long producerId,
            OrderStatus status)
    {
        log.info("Getting orders with status {} for producer {}", status, producerId);

        List<Order> orders = orderRepository.findByProducerIdAndStatusOrderByCreatedAtDesc(producerId, status);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderDTO> updateOrderStatusByProducer(Long orderId,
            OrderStatus status, Long producerId)
    {
        log.info("Producer {} updating order {} to status {}", producerId, orderId, status);

        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Verificar que al menos uno de los productos de la orden pertenece al productor
        boolean hasProducerProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getProducer().getId().equals(producerId));

        if (!hasProducerProduct) {
            throw new IllegalArgumentException("Esta orden no contiene productos del productor especificado");
        }

        // Validar transiciones de estado permitidas
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new IllegalArgumentException("Transición de estado no válida: " +
                    order.getStatus() + " -> " + status);
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return Optional.of(orderMapper.toDTO(updatedOrder));
    }

    @Override
    public ProducerSalesStatsDTO getProducerSalesStats(Long producerId)
    {
        log.info("Calculating sales stats for producer: {}", producerId);

        // Obtener todas las órdenes del productor
        List<Order> allOrders = orderRepository.findByProducerIdOrderByCreatedAtDesc(producerId);

        // Calcular estadísticas básicas
        Long totalOrders = (long) allOrders.size();
        Double totalRevenue = allOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Contar por estado
        Long pendingOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.PENDING ? 1 : 0)
                .sum();
        Long confirmedOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.CONFIRMED ? 1 : 0)
                .sum();
        Long preparingOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.PREPARING ? 1 : 0)
                .sum();
        Long shippedOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.SHIPPED ? 1 : 0)
                .sum();
        Long deliveredOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.DELIVERED ? 1 : 0)
                .sum();
        Long cancelledOrders = allOrders.stream()
                .mapToLong(order -> order.getStatus() == OrderStatus.CANCELLED ? 1 : 0)
                .sum();

        ProducerSalesStatsDTO stats = new ProducerSalesStatsDTO(
                totalOrders, totalRevenue, pendingOrders, confirmedOrders,
                preparingOrders, shippedOrders, deliveredOrders, cancelledOrders
        );

        // Calcular estadísticas adicionales
        calculateAdditionalStats(stats, allOrders);

        return stats;
    }

    @Override
    public List<OrderDTO> getTodayOrdersByProducerId(Long producerId)
    {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Order> todayOrders = orderRepository.findByProducerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                producerId, startOfDay, endOfDay);

        return todayOrders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
        case PENDING:
            return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
        case CONFIRMED:
            return newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
        case PREPARING:
            return newStatus == OrderStatus.SHIPPED;
        case SHIPPED:
            return newStatus == OrderStatus.DELIVERED;
        case DELIVERED:
        case CANCELLED:
            return false; // Estados finales
        default:
            return false;
        }
    }

    private void calculateAdditionalStats(ProducerSalesStatsDTO stats, List<Order> allOrders) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        List<Order> todayOrders = allOrders.stream()
                .filter(order -> order.getCreatedAt().isAfter(startOfDay))
                .collect(Collectors.toList());
        stats.setTodayOrders((long) todayOrders.size());
        stats.setTodayRevenue(todayOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum());

        List<Order> weekOrders = allOrders.stream()
                .filter(order -> order.getCreatedAt().isAfter(startOfWeek))
                .collect(Collectors.toList());
        stats.setThisWeekOrders((long) weekOrders.size());
        stats.setThisWeekRevenue(weekOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum());

        List<Order> monthOrders = allOrders.stream()
                .filter(order -> order.getCreatedAt().isAfter(startOfMonth))
                .collect(Collectors.toList());
        stats.setThisMonthOrders((long) monthOrders.size());
        stats.setThisMonthRevenue(monthOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum());

        if (!allOrders.isEmpty()) {
            stats.setLastOrderDate(allOrders.get(0).getCreatedAt());
        }
    }

    @Override
    @Transactional
    public Optional<OrderDTO> confirmDelivery(Long orderId, Long userId) {
        log.info("User {} confirming delivery for order {}", userId, orderId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Este pedido no pertenece al usuario");
        }

        // Solo se puede confirmar entrega si está en estado SHIPPED
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Solo se puede confirmar entrega de pedidos enviados. Estado actual: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return Optional.of(orderMapper.toDTO(updatedOrder));
    }

}