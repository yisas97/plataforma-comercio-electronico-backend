package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>
{
    Optional<Category> findByName(String name);
    List<Category> findByNameContainingIgnoreCase(String name);
}
