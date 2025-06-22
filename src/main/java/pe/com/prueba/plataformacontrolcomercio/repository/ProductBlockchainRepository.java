package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.blockchain.ProductBlockchain;

import java.util.Optional;

@Repository
public interface ProductBlockchainRepository
        extends JpaRepository<ProductBlockchain, Long>
{

    Optional<ProductBlockchain> findByProductId(Long productId);

    Optional<ProductBlockchain> findByBlockchainHash(String blockchainHash);

    boolean existsByProductId(Long productId);
}
