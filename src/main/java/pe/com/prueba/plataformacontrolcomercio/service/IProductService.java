package pe.com.prueba.plataformacontrolcomercio.service;

import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.util.List;
import java.util.Optional;

public interface IProductService
{
    List<Product> getAllProducts();
    List<Product> getProductsByProducerId(Long producerId);
    Optional<Product> getProductById(Long id);
    List<Product> searchProductsByName(String name);
    List<Product> searchProductsByNameAndProducerId(String name, Long producerId);

    List<Product> getProductsByCategoryIdAndProducerId(Long categoryId, Long producerId);

    List<Product> getProductsByTagId(Long tagId);
    List<Product> getProductsByTagIdAndProducerId(Long tagId, Long producerId);

    List<Product> getProductsByCategoryIdsAndTagIdsAndProducerId(
            List<Long> categoryIds, List<Long> tagIds, Long producerId);

    Product createProduct(Product product, Long producerId);
    Optional<Product> updateProduct(Long id, Product product, Long producerId);
    boolean deleteProduct(Long id, Long producerId);

    boolean addCategoryToProduct(Long productId, Long categoryId, Long producerId);
    boolean removeCategoryFromProduct(Long productId, Long categoryId, Long producerId);

    boolean addTagToProduct(Long productId, Long tagId, Long producerId);
    boolean removeTagFromProduct(Long productId, Long tagId, Long producerId);
}
