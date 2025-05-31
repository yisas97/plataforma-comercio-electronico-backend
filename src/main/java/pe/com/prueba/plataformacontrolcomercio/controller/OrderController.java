package pe.com.prueba.plataformacontrolcomercio.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.prueba.plataformacontrolcomercio.dto.CreateOrderRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderStatsDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.UpdateOrderStatusRequest;
import pe.com.prueba.plataformacontrolcomercio.service.order.IOrderService;
import pe.com.prueba.plataformacontrolcomercio.util.TokenUtils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final IOrderService orderService;
    private final TokenUtils tokenUtils;

    @Autowired
    public OrderController(IOrderService orderService, TokenUtils tokenUtils) {
        this.orderService = orderService;
        this.tokenUtils = tokenUtils;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            HttpServletRequest request) {

        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            OrderDTO createdOrder = orderService.createOrder(userId, createOrderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (IllegalArgumentException e) {
            log.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders(HttpServletRequest request) {
        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<OrderDTO> order = orderService.getOrderByIdAndUserId(orderId, userId);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Optional<OrderDTO> cancelledOrder = orderService.cancelOrder(orderId, userId);
            return cancelledOrder.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsDTO> getOrderStats(HttpServletRequest request) {
        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OrderStatsDTO stats = orderService.getOrderStatsByUserId(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/producer/orders")
    public ResponseEntity<List<OrderDTO>> getProducerOrders(HttpServletRequest request) {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderDTO> orders = orderService.getOrdersByProducerId(producerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest statusRequest,
            HttpServletRequest request) {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<OrderDTO> updatedOrder = orderService.updateOrderStatus(
                    orderId, statusRequest.getStatus(), producerId);
            return updatedOrder.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders(HttpServletRequest request) {
        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}