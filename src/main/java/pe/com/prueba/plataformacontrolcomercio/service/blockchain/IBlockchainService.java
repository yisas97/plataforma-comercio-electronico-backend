package pe.com.prueba.plataformacontrolcomercio.service.blockchain;

import pe.com.prueba.plataformacontrolcomercio.model.Producer;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.blockchain.ProductBlockchain;

import java.util.Optional;

public interface IBlockchainService
{
    ProductBlockchain createProductCertificate(
            Product product, Producer producer);

    Optional<ProductBlockchain> getCertificateByProductId(Long productId);

    boolean verifyCertificate(String blockchainHash);

    Optional<ProductBlockchain> getCertificateByHash(String blockchainHash);
}
