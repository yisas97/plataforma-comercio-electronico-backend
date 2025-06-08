package pe.com.prueba.plataformacontrolcomercio.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.prueba.plataformacontrolcomercio.dto.cart.AddToCartRequest;
import pe.com.prueba.plataformacontrolcomercio.dto.cart.CartItemDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.cart.UpdateCartItemRequest;
import pe.com.prueba.plataformacontrolcomercio.service.cart.ICartService;
import pe.com.prueba.plataformacontrolcomercio.util.TokenUtils;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Slf4j
public class CartController
{

    private final ICartService cartService;
    private final TokenUtils tokenUtils;

    @Autowired
    public CartController(ICartService cartService, TokenUtils tokenUtils)
    {
        this.cartService = cartService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(
            HttpServletRequest request)
    {
        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CartItemDTO> cartItems = cartService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(
            @Valid @RequestBody AddToCartRequest addRequest,
            HttpServletRequest request)
    {

        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            CartItemDTO cartItem = cartService.addToCart(userId,
                    addRequest.getProductId(), addRequest.getQuantity());
            return ResponseEntity.ok(cartItem);
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(@PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest updateRequest,
            HttpServletRequest request)
    {

        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            CartItemDTO updatedItem = cartService.updateCartItem(userId, itemId,
                    updateRequest.getQuantity());
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long itemId,
            HttpServletRequest request)
    {

        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try
        {
            boolean removed = cartService.removeFromCart(userId, itemId);
            return removed ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(HttpServletRequest request)
    {
        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCartItemCount(HttpServletRequest request)
    {
        Long userId = tokenUtils.getUserIdFromRequest(request);
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(count);
    }
}
