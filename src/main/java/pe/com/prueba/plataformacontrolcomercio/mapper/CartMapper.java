package pe.com.prueba.plataformacontrolcomercio.mapper;

import org.springframework.stereotype.Component;
import pe.com.prueba.plataformacontrolcomercio.dto.cart.CartItemDTO;
import pe.com.prueba.plataformacontrolcomercio.model.CartItem;

@Component
public class CartMapper {

    public CartItemDTO toDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());

        if (cartItem.getUser() != null) {
            dto.setUserId(cartItem.getUser().getId());
        }

        if (cartItem.getProduct() != null) {
            dto.setProductId(cartItem.getProduct().getId());
            dto.setProductName(cartItem.getProduct().getName());
            dto.setProductPrice(cartItem.getProduct().getPrice());
            // Si tienes campo image en Product, descomenta la siguiente línea:
            // dto.setProductImage(cartItem.getProduct().getImage());
        }

        dto.setSubtotal(cartItem.getSubtotal());

        return dto;
    }

    public CartItem toEntity(CartItemDTO dto) {
        if (dto == null) {
            return null;
        }

        CartItem cartItem = new CartItem();
        cartItem.setId(dto.getId());
        cartItem.setQuantity(dto.getQuantity());
        cartItem.setCreatedAt(dto.getCreatedAt());
        cartItem.setUpdatedAt(dto.getUpdatedAt());

        // Nota: User y Product deben ser establecidos por el servicio
        // ya que requieren consultas a la base de datos

        return cartItem;
    }
}