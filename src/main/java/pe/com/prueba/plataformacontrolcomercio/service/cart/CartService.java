package pe.com.prueba.plataformacontrolcomercio.service.cart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.dto.cart.CartItemDTO;
import pe.com.prueba.plataformacontrolcomercio.mapper.CartMapper;
import pe.com.prueba.plataformacontrolcomercio.model.CartItem;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.User;
import pe.com.prueba.plataformacontrolcomercio.repository.CartItemRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProductRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService implements ICartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Autowired
    public CartService(CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartMapper cartMapper) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    public List<CartItemDTO> getCartItemsByUserId(Long userId) {
        log.info("Getting cart items for user: {}", userId);

        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cartItems.stream()
                .map(cartMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemDTO addToCart(Long userId, Long productId, Integer quantity) {
        log.info("Adding product {} to cart for user {} with quantity {}", productId, userId, quantity);

        // Validar que el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el producto existe y tiene stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente. Stock disponible: " + product.getQuantity());
        }

        // Verificar si el producto ya está en el carrito
        Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // Si ya existe, actualizar la cantidad
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getQuantity() < newQuantity) {
                throw new IllegalArgumentException("Stock insuficiente. Stock disponible: " + product.getQuantity());
            }

            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Si no existe, crear nuevo item
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return cartMapper.toDTO(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemDTO updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item {} for user {} with quantity {}", cartItemId, userId, quantity);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item del carrito no encontrado"));

        // Verificar que el item pertenece al usuario
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Este item no pertenece al usuario");
        }

        // Validar stock disponible
        if (cartItem.getProduct().getQuantity() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente. Stock disponible: " +
                    cartItem.getProduct().getQuantity());
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        return cartMapper.toDTO(updatedCartItem);
    }

    @Override
    @Transactional
    public boolean removeFromCart(Long userId, Long cartItemId) {
        log.info("Removing cart item {} for user {}", cartItemId, userId);

        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            return false;
        }

        CartItem cartItem = cartItemOpt.get();

        // Verificar que el item pertenece al usuario
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Este item no pertenece al usuario");
        }

        cartItemRepository.delete(cartItem);
        return true;
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user {}", userId);
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    public Long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    @Override
    public Double getCartTotal(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }
}