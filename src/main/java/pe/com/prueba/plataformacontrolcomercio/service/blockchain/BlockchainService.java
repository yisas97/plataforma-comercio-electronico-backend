package pe.com.prueba.plataformacontrolcomercio.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.model.blockchain.ProductBlockchain;
import pe.com.prueba.plataformacontrolcomercio.repository.ProductBlockchainRepository;
import pe.com.prueba.plataformacontrolcomercio.util.BlockchainUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BlockchainService implements IBlockchainService{

    private final ProductBlockchainRepository blockchainRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BlockchainService(ProductBlockchainRepository blockchainRepository)
    {
        this.blockchainRepository = blockchainRepository;
    }

    /**
     * Crea un certificado blockchain para un producto
     */
    public ProductBlockchain createProductCertificate(Product product, Producer producer) {
        try {
            // Verificar si ya existe certificado
            if (blockchainRepository.existsByProductId(product.getId())) {
                log.info("Certificate already exists for product: {}", product.getId());
                return blockchainRepository.findByProductId(product.getId()).orElse(null);
            }

            // Crear datos del certificado
            Map<String, Object> certificateData = new HashMap<>();
            certificateData.put("productId", product.getId());
            certificateData.put("productName", product.getName());
            certificateData.put("productDescription", product.getDescription());
            certificateData.put("producerName", producer.getBusinessName());
            certificateData.put("producerLocation", producer.getLocation());
            certificateData.put("certificationDate", LocalDateTime.now().toString());
            certificateData.put("price", product.getPrice());
            certificateData.put("sku", product.getSku());
            certificateData.put("localSource", true);
            certificateData.put("verified", true);

            // Generar hash único
            String dataToHash = product.getId() + product.getName() + producer.getId() +
                    LocalDateTime.now().toString() + UUID.randomUUID().toString();
            String blockchainHash = generateHash(dataToHash);
            String transactionHash = "0x" + generateHash(blockchainHash + "transaction");

            // Crear el certificado
            ProductBlockchain blockchain = new ProductBlockchain();
            blockchain.setProductId(product.getId());
            blockchain.setBlockchainHash(blockchainHash);
            blockchain.setTransactionHash(transactionHash);
            blockchain.setCertificateData(objectMapper.writeValueAsString(certificateData));
            blockchain.setCreatedDate(LocalDateTime.now());
            blockchain.setIsVerified(true);

            ProductBlockchain saved = blockchainRepository.save(blockchain);
            log.info("Blockchain certificate created for product: {} with hash: {}",
                    product.getId(), blockchainHash);

            return saved;

        } catch (JsonProcessingException e) {
            log.error("Error creating blockchain certificate for product: {}", product.getId(), e);
            throw new RuntimeException("Error creating blockchain certificate", e);
        }
    }

    /**
     * Obtener certificado por ID de producto
     */
    public Optional<ProductBlockchain> getCertificateByProductId(Long productId) {
        return blockchainRepository.findByProductId(productId);
    }

    /**
     * Verificar certificado por hash
     */
    public boolean verifyCertificate(String blockchainHash) {
        Optional<ProductBlockchain> certificate = blockchainRepository.findByBlockchainHash(blockchainHash);
        return certificate.isPresent() && certificate.get().getIsVerified();
    }

    /**
     * Obtener certificado por hash
     */
    public Optional<ProductBlockchain> getCertificateByHash(String blockchainHash) {
        return blockchainRepository.findByBlockchainHash(blockchainHash);
    }

    /**
     * Generar hash SHA-256
     */
    private String generateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return BlockchainUtils.bytesToHex(hash).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 not available, using UUID fallback");
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

}