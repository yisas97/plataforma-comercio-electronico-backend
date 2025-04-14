package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;

import java.util.Optional;

@Repository
public interface ProducerRepository extends JpaRepository<Producer, Long>
{
    Optional<Producer> findByUserId(Long userId);
}