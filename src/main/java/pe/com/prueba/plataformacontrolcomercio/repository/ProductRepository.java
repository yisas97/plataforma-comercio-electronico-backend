package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.dto.producer.ProducerMarketplaceDTO;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;
import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByProducer(Producer producer);
    List<Product> findByProducerId(Long producerId);
    List<Product> findByNameContainingIgnoreCaseAndProducerId(String name, Long producerId);

    @Query("SELECT p FROM Product p JOIN p.productCategories pc WHERE pc.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p JOIN p.productCategories pc WHERE pc.category.id = :categoryId AND p.producer.id = :producerId")
    List<Product> findByCategoryIdAndProducerId(@Param("categoryId") Long categoryId, @Param("producerId") Long producerId);

    @Query("SELECT p FROM Product p JOIN p.productTags pt WHERE pt.tag.id = :tagId")
    List<Product> findByTagId(@Param("tagId") Long tagId);

    @Query("SELECT p FROM Product p JOIN p.productTags pt WHERE pt.tag.id = :tagId AND p.producer.id = :producerId")
    List<Product> findByTagIdAndProducerId(@Param("tagId") Long tagId, @Param("producerId") Long producerId);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.productCategories pc " +
            "JOIN p.productTags pt " +
            "WHERE pc.category.id IN :categoryIds " +
            "AND pt.tag.id IN :tagIds " +
            "AND p.producer.id = :producerId")
    List<Product> findByCategoryIdsAndTagIdsAndProducerId(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("tagIds") List<Long> tagIds,
            @Param("producerId") Long producerId);

    @Query("SELECT new pe.com.prueba.plataformacontrolcomercio.dto.producer.ProducerMarketplaceDTO(" +
            "p.producer.id, p.producer.businessName, p.producer.description, " +
            "p.producer.location, p.producer.phone, COUNT(p)) " +
            "FROM Product p " +
            "WHERE p.producer.approved = true AND p.quantity > 0 " +
            "GROUP BY p.producer.id, p.producer.businessName, p.producer.description, " +
            "p.producer.location, p.producer.phone " +
            "ORDER BY p.producer.businessName")
    List<ProducerMarketplaceDTO> findApprovedProducersWithStock();

    @Query("SELECT p FROM Product p WHERE p.producer.id = :producerId AND p.producer.approved = true")
    List<Product> findByProducerIdAndProducerApprovedTrue(Long producerId);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.producer.businessName) LIKE LOWER(CONCAT('%', :producerName, '%')) " +
            "AND p.producer.approved = true")
    List<Product> findByProducerBusinessNameContainingIgnoreCaseAndProducerApprovedTrue(@Param("producerName") String producerName);
}
