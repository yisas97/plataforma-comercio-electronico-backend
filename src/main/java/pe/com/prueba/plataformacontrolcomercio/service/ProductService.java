package pe.com.prueba.plataformacontrolcomercio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.model.Category;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService
{

    private final ProductRepository productRepository;
    private final ProducerRepository producerRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductTagRepository productTagRepository;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            ProducerRepository producerRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductTagRepository productTagRepository) {
        this.productRepository = productRepository;
        this.producerRepository = producerRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productTagRepository = productTagRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByProducerId(Long producerId) {
        return productRepository.findByProducerId(producerId);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Product> searchProductsByNameAndProducerId(String name, Long producerId) {
        return productRepository.findByNameContainingIgnoreCaseAndProducerId(name, producerId);
    }

    @Override
    public List<Product> getProductsByCategoryIdAndProducerId(Long categoryId, Long producerId) {
        return productRepository.findByCategoryIdAndProducerId(categoryId, producerId);
    }

    @Override
    public List<Product> getProductsByTagId(Long tagId) {
        return productRepository.findByTagId(tagId);
    }

    @Override
    public List<Product> getProductsByTagIdAndProducerId(Long tagId, Long producerId) {
        return productRepository.findByTagIdAndProducerId(tagId, producerId);
    }

    @Override
    public List<Product> getProductsByCategoryIdsAndTagIdsAndProducerId(
            List<Long> categoryIds, List<Long> tagIds, Long producerId) {
        return productRepository.findByCategoryIdsAndTagIdsAndProducerId(categoryIds, tagIds, producerId);
    }

    @Override
    public Product createProduct(Product product, Long producerId) {
        Optional<Producer> producerOpt = producerRepository.findById(producerId);
        if (producerOpt.isPresent()) {
            product.setProducer(producerOpt.get());
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            return productRepository.save(product);
        } else {
            throw new IllegalArgumentException("Productor no encontrado con ID: " + producerId);
        }
    }

    @Override
    public Optional<Product> updateProduct(Long id, Product productDetails, Long producerId) {
        Optional<Producer> producerOpt = producerRepository.findById(producerId);

        if (producerOpt.isEmpty()) {
            throw new IllegalArgumentException("Productor no encontrado con ID: " + producerId);
        }

        return productRepository.findById(id).map(existingProduct -> {
            // Verificar que el producto pertenece al productor
            if (!existingProduct.getProducer().getId().equals(producerId)) {
                throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
            }

            existingProduct.setName(productDetails.getName());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setQuantity(productDetails.getQuantity());
            existingProduct.setSku(productDetails.getSku());
            existingProduct.setUpdatedAt(LocalDateTime.now());
            return productRepository.save(existingProduct);
        });
    }

    @Override
    public boolean deleteProduct(Long id, Long producerId) {
        return productRepository.findById(id).map(product -> {
            if (!product.getProducer().getId().equals(producerId)) {
                throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
            }

            productRepository.delete(product);
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

        // Verificar que el producto pertenece al productor
        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        // Verificar si ya existe la relación
        if (productCategoryRepository.findByProductAndCategory(product, categoryOpt.get()).isPresent()) {
            return true; // Ya existe la relación, no hacemos nada
        }

        // Agregar la categoría al producto
        ProductCategory productCategory = new ProductCategory();
        productCategory.setProduct(product);
        productCategory.setCategory(categoryOpt.get());
        productCategory.setCreatedAt(LocalDateTime.now());
        productCategoryRepository.save(productCategory);

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

        // Verificar que el producto pertenece al productor
        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        // Eliminar la relación si existe
        Optional<ProductCategory> productCategoryOpt = productCategoryRepository.findByProductAndCategory(product, categoryOpt.get());
        if (productCategoryOpt.isPresent()) {
            productCategoryRepository.delete(productCategoryOpt.get());
            return true;
        }

        return false; // No existía la relación
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

        // Verificar que el producto pertenece al productor
        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        // Verificar si ya existe la relación
        if (productTagRepository.findByProductAndTag(product, tagOpt.get()).isPresent()) {
            return true; // Ya existe la relación, no hacemos nada
        }

        // Agregar la etiqueta al producto
        ProductTag productTag = new ProductTag();
        productTag.setProduct(product);
        productTag.setTag(tagOpt.get());
        productTag.setCreatedAt(LocalDateTime.now());
        productTagRepository.save(productTag);

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

        // Verificar que el producto pertenece al productor
        if (!product.getProducer().getId().equals(producerId)) {
            throw new IllegalArgumentException("Este producto no pertenece al productor especificado");
        }

        // Eliminar la relación si existe
        Optional<ProductTag> productTagOpt = productTagRepository.findByProductAndTag(product, tagOpt.get());
        if (productTagOpt.isPresent()) {
            productTagRepository.delete(productTagOpt.get());
            return true;
        }

        return false; // No existía la relación
    }
}
