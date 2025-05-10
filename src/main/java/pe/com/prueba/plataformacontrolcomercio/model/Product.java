package pe.com.prueba.plataformacontrolcomercio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"producer", "productCategories", "productTags"})
@EqualsAndHashCode(of = "id")
public class Product
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nombre del producto es obligatorio")
    private String name;

    @NotNull(message = "Precio es obligatorio")
    @Positive(message = "Precio debe ser positivo")
    private Double price;

    private String description;
    private Integer quantity;
    private String sku;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "producer_id", nullable = false)
    @JsonIgnoreProperties({"products", "user", "createdAt", "updatedAt"})
    private Producer producer;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ProductCategory> productCategories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ProductTag> productTags = new HashSet<>();


    public void addCategory(Category category) {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setProduct(this);
        productCategory.setCategory(category);
        productCategory.setCreatedAt(LocalDateTime.now());
        productCategories.add(productCategory);
        category.getProductCategories().add(productCategory);
    }

    public void removeCategory(Category category) {
        for (ProductCategory productCategory : new HashSet<>(productCategories)) {
            if (productCategory.getCategory().equals(category)) {
                productCategories.remove(productCategory);
                category.getProductCategories().remove(productCategory);
                productCategory.setProduct(null);
                productCategory.setCategory(null);
            }
        }
    }

    public void addTag(Tag tag) {
        ProductTag productTag = new ProductTag();
        productTag.setProduct(this);
        productTag.setTag(tag);
        productTag.setCreatedAt(LocalDateTime.now());
        productTags.add(productTag);
        tag.getProductTags().add(productTag);
    }

    public void removeTag(Tag tag) {
        for (ProductTag productTag : new HashSet<>(productTags)) {
            if (productTag.getTag().equals(tag)) {
                productTags.remove(productTag);
                tag.getProductTags().remove(productTag);
                productTag.setProduct(null);
                productTag.setTag(null);
            }
        }
    }
}
