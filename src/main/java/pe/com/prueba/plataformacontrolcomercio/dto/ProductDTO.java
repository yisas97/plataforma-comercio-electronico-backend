package pe.com.prueba.plataformacontrolcomercio.dto;

import lombok.Data;
import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private Integer quantity;
    private String sku;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long producerId;
    private Set<Long> categoryIds;
    private Set<Long> tagIds;

    // Constructor para convertir de Product a ProductDTO
    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.quantity = product.getQuantity();
        this.sku = product.getSku();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();

        // Obtener el ID del productor
        if (product.getProducer() != null) {
            this.producerId = product.getProducer().getId();
        }

        // Obtener IDs de categorías
        this.categoryIds = product.getProductCategories().stream()
                .map(pc -> pc.getCategory().getId())
                .collect(Collectors.toSet());

        // Obtener IDs de etiquetas
        this.tagIds = product.getProductTags().stream()
                .map(pt -> pt.getTag().getId())
                .collect(Collectors.toSet());
    }
}
