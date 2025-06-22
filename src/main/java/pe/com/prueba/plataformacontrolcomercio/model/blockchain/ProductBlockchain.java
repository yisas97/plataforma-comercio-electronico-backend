package pe.com.prueba.plataformacontrolcomercio.model.blockchain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_blockchain")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductBlockchain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "blockchain_hash", unique = true, nullable = false)
    private String blockchainHash;

    @Column(name = "transaction_hash")
    private String transactionHash;

    @Column(name = "certificate_data", columnDefinition = "TEXT")
    private String certificateData; // JSON con datos del certificado

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "is_verified")
    private Boolean isVerified = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonIgnore
    private Product product;
}