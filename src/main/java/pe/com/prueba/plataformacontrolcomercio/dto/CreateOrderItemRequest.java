package pe.com.prueba.plataformacontrolcomercio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderItemRequest {
    @NotNull(message = "ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "Cantidad es obligatoria")
    @Positive(message = "Cantidad debe ser positiva")
    private Integer quantity;
}