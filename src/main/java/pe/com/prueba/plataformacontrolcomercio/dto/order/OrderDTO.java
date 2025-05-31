package pe.com.prueba.plataformacontrolcomercio.dto.order;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.prueba.plataformacontrolcomercio.model.Order;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Double totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> orderItems;

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.shippingAddress = order.getShippingAddress();
        this.paymentMethod = order.getPaymentMethod();
        this.notes = order.getNotes();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

        if (order.getUser() != null) {
            this.userId = order.getUser().getId();
            this.userName = order.getUser().getName();
            this.userEmail = order.getUser().getEmail();
        }

        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }
}
