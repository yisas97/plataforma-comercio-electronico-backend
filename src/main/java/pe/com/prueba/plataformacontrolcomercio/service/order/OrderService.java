package pe.com.prueba.plataformacontrolcomercio.service.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.dto.CreateOrderItemRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.CreateOrderRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderStatsDTO;
import pe.com.prueba.plataformacontrolcomercio.mapper.OrderMapper;
import pe.com.prueba.plataformacontrolcomercio.model.Order;
import pe.com.prueba.plataformacontrolcomercio.model.OrderItem;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.User;
import pe.com.prueba.plataformacontrolcomercio.repository.CartItemRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartItemRepository cartItemRepository,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
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
}