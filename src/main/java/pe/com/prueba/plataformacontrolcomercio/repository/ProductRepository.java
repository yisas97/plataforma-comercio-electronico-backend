package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategory(String category);
}
