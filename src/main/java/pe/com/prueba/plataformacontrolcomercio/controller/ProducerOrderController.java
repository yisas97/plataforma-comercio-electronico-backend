package pe.com.prueba.plataformacontrolcomercio.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.UpdateOrderStatusRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.producer.ProducerSalesStatsDTO;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;
import pe.com.prueba.plataformacontrolcomercio.service.order.IOrderService;
import pe.com.prueba.plataformacontrolcomercio.util.TokenUtils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/producer/orders")
@Slf4j
public class ProducerOrderController {

    private final IOrderService orderService;
    private final TokenUtils tokenUtils;

    @Autowired
    public ProducerOrderController(IOrderService orderService, TokenUtils tokenUtils) {
        this.orderService = orderService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getProducerOrders(
            HttpServletRequest request) {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Getting orders for producer: {}", producerId);
        List<OrderDTO> orders = orderService.getOrdersByProducerId(producerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getProducerOrdersByStatus(
            @PathVariable OrderStatus status,
            HttpServletRequest request) {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Getting orders with status {} for producer: {}", status, producerId);
        List<OrderDTO> orders = orderService.getOrdersByProducerIdAndStatus(producerId, status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest statusRequest,
            HttpServletRequest request) {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Producer {} updating order {} to status {}", producerId, orderId, statusRequest.getStatus());

        try {
            Optional<OrderDTO> updatedOrder = orderService.updateOrderStatusByProducer(
                    orderId, statusRequest.getStatus(), producerId);

            return updatedOrder.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<OrderDTO> confirmedOrder = orderService.updateOrderStatusByProducer(
                    orderId, OrderStatus.CONFIRMED, producerId);

            return confirmedOrder.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/prepare")
    public ResponseEntity<OrderDTO> prepareOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<OrderDTO> preparingOrder = orderService.updateOrderStatusByProducer(
                    orderId, OrderStatus.PREPARING, producerId);

            return preparingOrder.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<OrderDTO> shipOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<OrderDTO> shippedOrder = orderService.updateOrderStatusByProducer(
                    orderId, OrderStatus.SHIPPED, producerId);

            return shippedOrder.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ProducerSalesStatsDTO> getSalesStats(HttpServletRequest request) {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Getting sales stats for producer: {}", producerId);
        ProducerSalesStatsDTO stats = orderService.getProducerSalesStats(producerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<OrderDTO>> getDailyOrdersSummary(HttpServletRequest request) {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderDTO> todayOrders = orderService.getTodayOrdersByProducerId(producerId);
        return ResponseEntity.ok(todayOrders);
    }
}