package pe.com.prueba.plataformacontrolcomercio.service;

import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.util.List;
import java.util.Optional;

public interface IProductService
{
    List<Product> getAllProducts();

    Optional<Product> getProductById(Long id);

    List<Product> searchProductsByName(String name);

    List<Product> getProductsByCategory(String category);
    Product createProduct(Product product);

    Optional<Product> updateProduct(Long id, Product productDetails);

    boolean deleteProduct(Long id);
}
