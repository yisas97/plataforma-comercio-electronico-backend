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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.prueba.plataformacontrolcomercio.dto.ProductDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.producer.ProducerMarketplaceDTO;
import pe.com.prueba.plataformacontrolcomercio.exception.ProductDeletionException;
import pe.com.prueba.plataformacontrolcomercio.mapper.ProductMapper;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.blockchain.ProductBlockchain;
import pe.com.prueba.plataformacontrolcomercio.service.IProductService;
import pe.com.prueba.plataformacontrolcomercio.service.blockchain.BlockchainService;
import pe.com.prueba.plataformacontrolcomercio.service.ia.IAIClientService;
import pe.com.prueba.plataformacontrolcomercio.util.TokenUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController
{

    private final IProductService productService;
    private final TokenUtils tokenUtils;
    private final ProductMapper productMapper;
    private final BlockchainService blockchainService;
    private final IAIClientService aiClientService;

    @Autowired
    public ProductController(IProductService productService,
            TokenUtils tokenUtils, ProductMapper productMapper,
            BlockchainService blockchainService,
            IAIClientService aiClientService)
    {
        this.productService = productService;
        this.tokenUtils = tokenUtils;
        this.productMapper = productMapper;
        this.blockchainService = blockchainService;
        this.aiClientService = aiClientService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            HttpServletRequest request)
    {
        String role = tokenUtils.getRoleFromRequest(request);

        if ("ROLE_ADMIN".equals(role))
        {
            List<ProductDTO> products = productService.getAllProducts().stream()
                    .map(productMapper::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(products);
        } else
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getMyProducts(
            HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("getMyProducts producer id: " + producerId);
        List<ProductDTO> products = productService.getProductsByProducerId(
                        producerId).stream().map(productMapper::toDTO)
                .collect(Collectors.toList());
        log.info("getMyProducts products final: " + products);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id,
            HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Product> product = productService.getProductById(id);
        log.info("getProductById: " + product);

        if (product.isPresent() && product.get().getProducer().getId()
                .equals(producerId))
        {
            return ResponseEntity.ok(productMapper.toDTO(product.get()));
        } else
        {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam String name, HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ProductDTO> products = productService.searchProductsByNameAndProducerId(
                        name, producerId).stream().map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(
            @PathVariable Long categoryId, HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                productService.getProductsByCategoryIdAndProducerId(categoryId,
                        producerId));
    }

    @GetMapping("/by-tag/{tagId}")
    public ResponseEntity<List<Product>> getProductsByTagId(
            @PathVariable Long tagId, HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                productService.getProductsByTagIdAndProducerId(tagId,
                        producerId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> tagIds,
            HttpServletRequest request)
    {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if ((categoryIds == null || categoryIds.isEmpty()) && (tagIds == null || tagIds.isEmpty()))
        {
            return ResponseEntity.ok(
                    productService.getProductsByProducerId(producerId));
        }

        if (tagIds == null || tagIds.isEmpty())
        {
            return ResponseEntity.ok(
                    productService.getProductsByCategoryIdAndProducerId(
                            categoryIds.get(0), producerId));
        }

        if (categoryIds == null || categoryIds.isEmpty())
        {
            return ResponseEntity.ok(
                    productService.getProductsByTagIdAndProducerId(
                            tagIds.get(0), producerId));
        }

        return ResponseEntity.ok(
                productService.getProductsByCategoryIdsAndTagIdsAndProducerId(
                        categoryIds, tagIds, producerId));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productDTO.setProducerId(producerId);

        try
        {
            ProductDTO createdProduct = productService.createProduct(
                    productDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdProduct);
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO,
            HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productDTO.setId(id);
        productDTO.setProducerId(producerId);

        try
        {
            return productService.updateProduct(productDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable Long id, HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            boolean deleted = productService.deleteProduct(id, producerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto eliminado correctamente");
            response.put("deleted", deleted);

            return ResponseEntity.ok(response);

        } catch (ProductDeletionException e)
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", e.getErrorCode());
            response.put("markedInactive", e.canMarkInactive());

            return ResponseEntity.ok(
                    response); // 200 porque la operación fue "exitosa"

        } catch (IllegalArgumentException e)
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", "FORBIDDEN");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateProduct(
            @PathVariable Long id, HttpServletRequest request)
    {
        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            boolean reactivated = productService.reactivateProduct(id,
                    producerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto reactivado correctamente");
            response.put("reactivated", reactivated);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e)
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @PostMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Void> addCategoryToProduct(
            @PathVariable Long productId, @PathVariable Long categoryId,
            HttpServletRequest request)
    {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            boolean success = productService.addCategoryToProduct(productId,
                    categoryId, producerId);
            return success ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Void> removeCategoryFromProduct(
            @PathVariable Long productId, @PathVariable Long categoryId,
            HttpServletRequest request)
    {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            boolean success = productService.removeCategoryFromProduct(
                    productId, categoryId, producerId);
            return success ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{productId}/tags/{tagId}")
    public ResponseEntity<Void> addTagToProduct(@PathVariable Long productId,
            @PathVariable Long tagId, HttpServletRequest request)
    {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            boolean success = productService.addTagToProduct(productId, tagId,
                    producerId);
            return success ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{productId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromProduct(
            @PathVariable Long productId, @PathVariable Long tagId,
            HttpServletRequest request)
    {

        Long producerId = tokenUtils.getProducerIdFromRequest(request);
        if (producerId == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            boolean success = productService.removeTagFromProduct(productId,
                    tagId, producerId);
            return success ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/marketplace")
    public ResponseEntity<List<ProductDTO>> getMarketplaceProducts()
    {
        log.info("Getting all products for marketplace");

        List<ProductDTO> products = productService.getAllProducts().stream()
                .filter(product -> product.getQuantity() > 0)
                .map(productMapper::toDTO).collect(Collectors.toList());

        log.info("Found {} products for marketplace", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/marketplace/{id}")
    public ResponseEntity<ProductDTO> getMarketplaceProductById(
            @PathVariable Long id, HttpServletRequest request)
    {
        log.info("Getting marketplace product with id: {}", id);

        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent() && product.get().getQuantity() > 0) {

            Long userId = getUserIdFromRequest(request);
            if (userId != null) {
                log.info("Tracking VIEW interaction for user {} on product {}", userId, id);
                aiClientService.trackInteraction(userId, id, "VIEW");
            }

            return ResponseEntity.ok(productMapper.toDTO(product.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/marketplace/search")
    public ResponseEntity<List<ProductDTO>> searchMarketplaceProducts(
            @RequestParam String name)
    {
        log.info("Searching marketplace products with name: {}", name);

        List<ProductDTO> products = productService.searchProductsByName(name)
                .stream().filter(product -> product.getQuantity() > 0)
                .map(productMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/marketplace/by-category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getMarketplaceProductsByCategory(
            @PathVariable Long categoryId)
    {
        log.info("Getting marketplace products by category: {}", categoryId);

        List<ProductDTO> products = productService.getProductsByCategoryId(
                        categoryId).stream()
                .filter(product -> product.getQuantity() > 0)
                .map(productMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/marketplace/by-tag/{tagId}")
    public ResponseEntity<List<ProductDTO>> getMarketplaceProductsByTag(
            @PathVariable Long tagId)
    {
        log.info("Getting marketplace products by tag: {}", tagId);

        List<ProductDTO> products = productService.getProductsByTagId(tagId)
                .stream().filter(product -> product.getQuantity() > 0)
                .map(productMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/marketplace/producers")
    public ResponseEntity<List<ProducerMarketplaceDTO>> getMarketplaceProducers()
    {
        log.info("Getting all approved producers for marketplace");
        List<ProducerMarketplaceDTO> producers = productService.getApprovedProducersWithStock();

        log.info("Found {} approved producers with stock for marketplace",
                producers.size());
        return ResponseEntity.ok(producers);
    }

    @GetMapping("/marketplace/producers/{producerId}/products")
    public ResponseEntity<List<ProductDTO>> getProductsByProducerForMarketplace(
            @PathVariable Long producerId)
    {
        log.info("Getting products for producer {} in marketplace", producerId);

        List<ProductDTO> products = productService.getProductsByProducerIdForMarketplace(
                        producerId).stream()
                .filter(product -> product.getQuantity() > 0) // Solo productos con stock
                .map(productMapper::toDTO).collect(Collectors.toList());

        log.info("Found {} products for producer {} in marketplace",
                products.size(), producerId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/marketplace/search/producer")
    public ResponseEntity<List<ProductDTO>> searchProductsByProducerName(
            @RequestParam String producerName)
    {
        log.info("Searching products by producer name: {}", producerName);

        List<ProductDTO> products = productService.searchProductsByProducerName(
                        producerName).stream()
                .filter(product -> product.getQuantity() > 0)
                .map(productMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/certificate")
    public ResponseEntity<ProductBlockchain> getProductCertificate(
            @PathVariable Long id, HttpServletRequest request)
    {

        Optional<ProductBlockchain> certificate = blockchainService.getCertificateByProductId(
                id);

        return certificate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/marketplace/{id}/certificate")
    public ResponseEntity<ProductBlockchain> getMarketplaceCertificate(
            @PathVariable Long id)
    {
        log.info("Getting certificate for marketplace product: {}", id);

        Optional<ProductBlockchain> certificate = blockchainService.getCertificateByProductId(
                id);

        return certificate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/certificate/verify/{hash}")
    public ResponseEntity<Map<String, Object>> verifyCertificate(
            @PathVariable String hash)
    {
        log.info("Verifying certificate with hash: {}", hash);

        boolean isValid = blockchainService.verifyCertificate(hash);
        Optional<ProductBlockchain> certificate = blockchainService.getCertificateByHash(
                hash);

        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);
        if (certificate.isPresent())
        {
            response.put("certificate", certificate.get());
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductDTO>> getRecommendationsForUser(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return getPopularRecommendations(limit);
        }

        try {
            log.info("Getting AI recommendations for user: {}", userId);

            String url = aiClientService.getAiServiceUrl() + "/api/ai/recommendations/" + userId + "?limit=" + limit;
            ProductDTO[] recommendations = aiClientService.getRestTemplate().getForObject(url, ProductDTO[].class);

            if (recommendations != null && recommendations.length > 0) {
                return ResponseEntity.ok(List.of(recommendations));
            }

        } catch (Exception e) {
            log.error("Error getting recommendations for user {}: {}", userId, e.getMessage());
        }

        return getPopularRecommendations(limit);
    }

    @GetMapping("/recommendations/popular")
    public ResponseEntity<List<ProductDTO>> getPopularRecommendations(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            log.info("Getting popular recommendations from AI service");

            String url = aiClientService.getAiServiceUrl() + "/api/ai/recommendations/popular?limit=" + limit;
            ProductDTO[] recommendations = aiClientService.getRestTemplate().getForObject(url, ProductDTO[].class);

            if (recommendations != null && recommendations.length > 0) {
                return ResponseEntity.ok(List.of(recommendations));
            }

        } catch (Exception e) {
            log.error("Error getting popular recommendations: {}", e.getMessage());
        }

        // Fallback final: productos del marketplace con más stock
        List<ProductDTO> fallbackProducts = productService.getAllProducts().stream()
                .filter(product -> product.getQuantity() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getQuantity(), p1.getQuantity()))
                .limit(limit)
                .map(productMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(fallbackProducts);
    }

    @PostMapping("/marketplace/{id}/favorite")
    public ResponseEntity<Map<String, Object>> favoriteProduct(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Product> product = productService.getProductById(id);
        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        log.info("Tracking FAVORITE interaction for user {} on product {}", userId, id);
        aiClientService.trackInteraction(userId, id, "FAVORITE");

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Producto agregado a favoritos",
                "productId", id
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/marketplace/{id}/detail")
    public ResponseEntity<ProductDTO> getMarketplaceProductDetail(
            @PathVariable Long id,
            HttpServletRequest request) {

        log.info("Client viewing marketplace product detail: {}", id);

        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent() && product.get().getQuantity() > 0) {
            ProductDTO productDTO = productMapper.toDTO(product.get());

            Long userId = getUserIdFromRequest(request);
            if (userId != null) {
                log.info("Tracking VIEW interaction for user {} on product {}", userId, id);
                aiClientService.trackInteraction(userId, id, "VIEW");
            }

            return ResponseEntity.ok(productDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            return tokenUtils.getUserIdFromRequest(request);
        } catch (Exception e) {
            log.debug("No user authenticated in request");
            return null;
        }
    }
}
