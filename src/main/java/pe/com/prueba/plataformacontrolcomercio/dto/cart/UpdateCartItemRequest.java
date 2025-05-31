package pe.com.prueba.plataformacontrolcomercio.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull(message = "Cantidad es obligatoria")
    @Positive(message = "Cantidad debe ser positiva")
    private Integer quantity;
}