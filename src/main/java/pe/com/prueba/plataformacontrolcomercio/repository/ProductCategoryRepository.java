package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Category;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.ProductCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>
{
    List<ProductCategory> findByProduct(Product product);
    List<ProductCategory> findByCategory(Category category);
    Optional<ProductCategory> findByProductAndCategory(Product product, Category category);
    void deleteByProductAndCategory(Product product, Category category);
}