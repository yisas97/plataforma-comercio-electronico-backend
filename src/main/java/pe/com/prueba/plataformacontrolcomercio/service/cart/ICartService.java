package pe.com.prueba.plataformacontrolcomercio.service.cart;

import pe.com.prueba.plataformacontrolcomercio.dto.cart.CartItemDTO;

import java.util.List;

public interface ICartService
{

    List<CartItemDTO> getCartItemsByUserId(Long userId);

    CartItemDTO addToCart(Long userId, Long productId, Integer quantity);

    CartItemDTO updateCartItem(Long userId, Long cartItemId, Integer quantity);

    boolean removeFromCart(Long userId, Long cartItemId);

    void clearCart(Long userId);

    Long getCartItemCount(Long userId);

    Double getCartTotal(Long userId);
}