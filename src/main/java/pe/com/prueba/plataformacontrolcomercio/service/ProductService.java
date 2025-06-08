package pe.com.prueba.plataformacontrolcomercio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.dto.ProductDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.producer.ProducerMarketplaceDTO;
import pe.com.prueba.plataformacontrolcomercio.mapper.ProductMapper;
import pe.com.prueba.plataformacontrolcomercio.model.Category;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.ProductCategory;
import pe.com.prueba.plataformacontrolcomercio.model.ProductTag;
import pe.com.prueba.plataformacontrolcomercio.model.Tag;
import pe.com.prueba.plataformacontrolcomercio.repository.CategoryRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProducerRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProductCategoryRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProductRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProductTagRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.TagRepository;
import pe.com.prueba.plataformacontrolcomercio.service.cache.CacheService;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductService implements IProductService
{

    private final ProductRepository productRepository;
    private final ProducerRepository producerRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductMapper productMapper;
    private final CacheService cacheService;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            ProducerRepository producerRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductTagRepository productTagRepository,
            ProductMapper productMapper,
            CacheService cacheService) {
        this.productRepository = productRepository;
        this.producerRepository = producerRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productTagRepository = productTagRepository;
        this.productMapper = productMapper;
        this.cacheService = cacheService;
    }

    @Override
    public List<Product> getAllProducts() {
        String cacheKey = "products:all";
        return cacheService.getFromCache(cacheKey, List.class,
                productRepository::findAll);
    }

    @Override
    public List<Product> getProductsByProducerId(Long producerId) {
        String cacheKey = "products:producer:" + producerId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByProducerId(producerId));
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        String cacheKey = "product:" + id;
        Product cachedProduct = cacheService.getFromCache(cacheKey, Product.class,
                () -> productRepository.findById(id).orElse(null));
        return Optional.ofNullable(cachedProduct);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        String cacheKey = "products:search:name:" + name.toLowerCase();
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByNameContainingIgnoreCase(name));
    }

    @Override
    public List<Product> searchProductsByNameAndProducerId(String name, Long producerId) {
        String cacheKey = "products:search:name:" + name.toLowerCase() + ":producer:" + producerId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByNameContainingIgnoreCaseAndProducerId(name, producerId));
    }

    @Override
    public List<Product> getProductsByCategoryIdAndProducerId(Long categoryId, Long producerId) {
        String cacheKey = "products:category:" + categoryId + ":producer:" + producerId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByCategoryIdAndProducerId(categoryId, producerId));
    }

    @Override
    public List<Product> getProductsByTagId(Long tagId) {
        String cacheKey = "products:tag:" + tagId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByTagId(tagId));
    }

    @Override
    public List<Product> getProductsByTagIdAndProducerId(Long tagId, Long producerId) {
        String cacheKey = "products:tag:" + tagId + ":producer:" + producerId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByTagIdAndProducerId(tagId, producerId));
    }

    @Override
    public List<Product> getProductsByCategoryIdsAndTagIdsAndProducerId(
            List<Long> categoryIds, List<Long> tagIds, Long producerId) {
        String cacheKey = "products:complex:" + categoryIds.toString() + ":tags:" + tagIds.toString() + ":producer:" + producerId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByCategoryIdsAndTagIdsAndProducerId(categoryIds, tagIds, producerId));
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (!producerRepository.existsById(productDTO.getProducerId())) {
            throw new IllegalArgumentException("Productor no encontrado con ID: " + productDTO.getProducerId());
        }

        Product product = productMapper.toEntity(productDTO);
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product savedProduct = productRepository.save(product);

        cacheService.invalidatePattern("products:all");
        cacheService.invalidatePattern("products:producer:" + savedProduct.getProducer().getId());
        cacheService.invalidatePattern("products:search:*");
        log.info("Cache invalidated after creating product: {}", savedProduct.getId());

        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public Optional<ProductDTO> updateProduct(ProductDTO productDTO) {
        if (!producerRepository.existsById(productDTO.getProducerId())) {
            throw new IllegalArgumentException("Productor no encontrado con ID: " + productDTO.getProducerId());
        }

        return productRepository.findById(productDTO.getId())
                .map(existingProduct -> {
                    if (!existingProduct.getProducer().getId().equals(productDTO.getProducerId())) {
                        throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
                    }

                    LocalDateTime createdAt = existingProduct.getCreatedAt();
                    Product updatedProduct = productMapper.toEntity(productDTO);
                    updatedProduct.setCreatedAt(createdAt);
                    updatedProduct.setUpdatedAt(LocalDateTime.now());

                    Product savedProduct = productRepository.save(updatedProduct);

                    cacheService.invalidateCache("product:" + savedProduct.getId());
                    cacheService.invalidatePattern("products:all");
                    cacheService.invalidatePattern("products:producer:" + savedProduct.getProducer().getId());
                    cacheService.invalidatePattern("products:search:*");
                    log.info("Cache invalidated after updating product: {}", savedProduct.getId());

                    return productMapper.toDTO(savedProduct);
                });
    }

    @Override
    public boolean deleteProduct(Long id, Long producerId) {
        return productRepository.findById(id).map(product -> {
            if (!product.getProducer().getId().equals(producerId)) {
                throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
            }

            productRepository.delete(product);

            cacheService.invalidateCache("product:" + id);
            cacheService.invalidatePattern("products:all");
            cacheService.invalidatePattern("products:producer:" + producerId);
            cacheService.invalidatePattern("products:search:*");
            log.info("Cache invalidated after deleting product: {}", id);

            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean addCategoryToProduct(Long productId, Long categoryId, Long producerId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);

        if (productOpt.isEmpty() || categoryOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        if (productCategoryRepository.findByProductAndCategory(product, categoryOpt.get()).isPresent()) {
            return true;
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setProduct(product);
        productCategory.setCategory(categoryOpt.get());
        productCategory.setCreatedAt(LocalDateTime.now());
        productCategoryRepository.save(productCategory);

        cacheService.invalidateCache("product:" + productId);
        cacheService.invalidatePattern("products:category:" + categoryId + "*");
        cacheService.invalidatePattern("products:complex:*");
        log.info("Cache invalidated after adding category {} to product: {}", categoryId, productId);

        return true;
    }

    @Override
    @Transactional
    public boolean removeCategoryFromProduct(Long productId, Long categoryId, Long producerId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);

        if (productOpt.isEmpty() || categoryOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        Optional<ProductCategory> productCategoryOpt = productCategoryRepository.findByProductAndCategory(product, categoryOpt.get());
        if (productCategoryOpt.isPresent()) {
            productCategoryRepository.delete(productCategoryOpt.get());

            cacheService.invalidateCache("product:" + productId);
            cacheService.invalidatePattern("products:category:" + categoryId + "*");
            cacheService.invalidatePattern("products:complex:*");
            log.info("Cache invalidated after removing category {} from product: {}", categoryId, productId);

            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean addTagToProduct(Long productId, Long tagId, Long producerId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Tag> tagOpt = tagRepository.findById(tagId);

        if (productOpt.isEmpty() || tagOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        if (productTagRepository.findByProductAndTag(product, tagOpt.get()).isPresent()) {
            return true;
        }

        ProductTag productTag = new ProductTag();
        productTag.setProduct(product);
        productTag.setTag(tagOpt.get());
        productTag.setCreatedAt(LocalDateTime.now());
        productTagRepository.save(productTag);

        cacheService.invalidateCache("product:" + productId);
        cacheService.invalidatePattern("products:tag:" + tagId + "*");
        cacheService.invalidatePattern("products:complex:*");
        log.info("Cache invalidated after adding tag {} to product: {}", tagId, productId);

        return true;
    }

    @Override
    @Transactional
    public boolean removeTagFromProduct(Long productId, Long tagId, Long producerId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Tag> tagOpt = tagRepository.findById(tagId);

        if (productOpt.isEmpty() || tagOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        Optional<ProductTag> productTagOpt = productTagRepository.findByProductAndTag(product, tagOpt.get());
        if (productTagOpt.isPresent()) {
            productTagRepository.delete(productTagOpt.get());

            cacheService.invalidateCache("product:" + productId);
            cacheService.invalidatePattern("products:tag:" + tagId + "*");
            cacheService.invalidatePattern("products:complex:*");
            log.info("Cache invalidated after removing tag {} from product: {}", tagId, productId);

            return true;
        }

        return false;
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        String cacheKey = "products:category:" + categoryId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<ProducerMarketplaceDTO> getApprovedProducersWithStock() {
        String cacheKey = "producers:approved:with-stock";
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findApprovedProducersWithStock());
    }

    @Override
    public List<Product> getProductsByProducerIdForMarketplace(Long producerId) {
        String cacheKey = "products:marketplace:producer:" + producerId;
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByProducerIdAndProducerApprovedTrue(producerId));
    }

    @Override
    public List<Product> searchProductsByProducerName(String producerName) {
        String cacheKey = "products:search:producer-name:" + producerName.toLowerCase();
        return cacheService.getFromCache(cacheKey, List.class,
                () -> productRepository.findByProducerBusinessNameContainingIgnoreCaseAndProducerApprovedTrue(producerName));
    }

}
