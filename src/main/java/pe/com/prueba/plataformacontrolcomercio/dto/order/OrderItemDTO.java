package pe.com.prueba.plataformacontrolcomercio.dto.order;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.prueba.plataformacontrolcomercio.model.OrderItem;

@Data
@NoArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private Double price;
    private Double subtotal;

    public OrderItemDTO(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
        this.subtotal = orderItem.getSubtotal();

        if (orderItem.getProduct() != null) {
            this.productId = orderItem.getProduct().getId();
            this.productName = orderItem.getProduct().getName();
            // this.productImage = orderItem.getProduct().getImage();
        }
    }
}
