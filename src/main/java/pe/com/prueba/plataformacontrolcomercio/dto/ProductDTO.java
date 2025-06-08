package pe.com.prueba.plataformacontrolcomercio.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProductDTO
{
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

    private String image;
    private String category;
    private Double rating;
    private String inventoryStatus;
    private Boolean active;
    private Integer stock;

    private String producerName;
    private String producerLocation;

    public ProductDTO(Product product)
    {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.quantity = product.getQuantity();
        this.sku = product.getSku();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();

        this.stock = product.getQuantity();
        this.active = true;
        this.inventoryStatus = getInventoryStatus(product.getQuantity());
        this.rating = 4.5;
        this.image = product.getImage() != null ?
                product.getImage() :
                "product-placeholder.jpg";

        if (product.getProducer() != null)
        {
            this.producerId = product.getProducer().getId();
            this.producerName = product.getProducer().getBusinessName();
            this.producerLocation = product.getProducer().getLocation();
        }

        this.categoryIds = product.getProductCategories().stream()
                .map(pc -> pc.getCategory().getId())
                .collect(Collectors.toSet());

        this.category = product.getProductCategories().stream().findFirst()
                .map(pc -> pc.getCategory().getName()).orElse("Sin categoría");

        this.tagIds = product.getProductTags().stream()
                .map(pt -> pt.getTag().getId()).collect(Collectors.toSet());
    }

    private String getInventoryStatus(Integer quantity)
    {
        if (quantity == null || quantity == 0)
        {
            return "AGOTADO";
        } else if (quantity < 10)
        {
            return "BAJO STOCK";
        } else
        {
            return "EN STOCK";
        }
    }
}
