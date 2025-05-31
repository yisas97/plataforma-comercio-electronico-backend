package pe.com.prueba.plataformacontrolcomercio.service.order;

import pe.com.prueba.plataformacontrolcomercio.dto.CreateOrderRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderStatsDTO;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface IOrderService {

    OrderDTO createOrder(Long userId, CreateOrderRequest createOrderRequest);

    List<OrderDTO> getOrdersByUserId(Long userId);

    Optional<OrderDTO> getOrderByIdAndUserId(Long orderId, Long userId);

    Optional<OrderDTO> cancelOrder(Long orderId, Long userId);

    OrderStatsDTO getOrderStatsByUserId(Long userId);

    // Para productores
    List<OrderDTO> getOrdersByProducerId(Long producerId);

    Optional<OrderDTO> updateOrderStatus(Long orderId, OrderStatus status, Long producerId);

    // Para administradores
    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByStatus(OrderStatus status);
}
