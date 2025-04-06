package pe.com.prueba.plataformacontrolcomercio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    private String category;

    private String sku;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
