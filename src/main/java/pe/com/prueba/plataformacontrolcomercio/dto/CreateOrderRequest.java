package pe.com.prueba.plataformacontrolcomercio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "Dirección de envío es obligatoria")
    private String shippingAddress;

    @NotBlank(message = "Método de pago es obligatorio")
    private String paymentMethod;

    private String notes;

    @NotEmpty(message = "Los items del pedido son obligatorios")
    private List<CreateOrderItemRequest> items;
}