package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.ProductTag;
import pe.com.prueba.plataformacontrolcomercio.model.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long>
{
    List<ProductTag> findByProduct(Product product);
    List<ProductTag> findByTag(Tag tag);
    Optional<ProductTag> findByProductAndTag(Product product, Tag tag);
    void deleteByProductAndTag(Product product, Tag tag);
}