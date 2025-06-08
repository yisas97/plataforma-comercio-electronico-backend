package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProducerRepository extends JpaRepository<Producer, Long>
{
    Optional<Producer> findByUserId(Long userId);

    @Query("SELECT p FROM Producer p WHERE p.approved = true")
    List<Producer> findByApprovedTrue();

    @Query("SELECT p FROM Producer p WHERE LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Producer> findByLocationContainingIgnoreCase(
            @Param("location") String location);

    @Query("SELECT COUNT(p) FROM Producer p WHERE p.approved = true")
    Long countByApprovedTrue();

    @Query("SELECT p FROM Producer p WHERE p.approved = true AND LOWER(p.businessName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Producer> findApprovedByBusinessNameContaining(
            @Param("name") String name);

    @Query("SELECT p FROM Producer p WHERE p.approved = false")
    List<Producer> findPendingApproval();

    @Query("SELECT DISTINCT p FROM Producer p JOIN p.products pr WHERE pr.quantity > 0 AND p.approved = true")
    List<Producer> findApprovedWithStock();
}