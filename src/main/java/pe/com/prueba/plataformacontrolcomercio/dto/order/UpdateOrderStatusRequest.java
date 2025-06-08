package pe.com.prueba.plataformacontrolcomercio.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;

@Data
public class UpdateOrderStatusRequest
{
    @NotBlank(message = "Estado es obligatorio")
    private OrderStatus status;
}

