package pe.com.prueba.plataformacontrolcomercio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "producers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "products"})
public class Producer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nombre de negocio es obligatorio")
    private String businessName;

    @NotBlank(message = "Descripción es obligatoria")
    private String description;

    @NotBlank(message = "Ubicación es obligatoria")
    private String location;

    @NotBlank(message = "Teléfono es obligatorio")
    private String phone;

    @NotNull(message = "Estado de aprobación es obligatorio")
    private boolean approved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"producer", "password", "createdAt", "updatedAt", "verified"})
    private User user;

    @OneToMany(mappedBy = "producer")
    @JsonIgnore
    private List<Product> products;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producer producer = (Producer) o;
        return Objects.equals(id, producer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}