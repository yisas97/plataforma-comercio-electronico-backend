package pe.com.prueba.plataformacontrolcomercio.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.prueba.plataformacontrolcomercio.model.CartItem;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CartItemDTO
{
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double productPrice;
    private Integer quantity;
    private Double subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartItemDTO(CartItem cartItem)
    {
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        this.createdAt = cartItem.getCreatedAt();
        this.updatedAt = cartItem.getUpdatedAt();

        if (cartItem.getUser() != null)
        {
            this.userId = cartItem.getUser().getId();
        }

        if (cartItem.getProduct() != null)
        {
            this.productId = cartItem.getProduct().getId();
            this.productName = cartItem.getProduct().getName();
            this.productPrice = cartItem.getProduct().getPrice();
            // this.productImage = cartItem.getProduct().getImage();
        }

        this.subtotal = cartItem.getSubtotal();
    }
}