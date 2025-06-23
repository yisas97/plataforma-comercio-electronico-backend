package pe.com.prueba.plataformacontrolcomercio.dto.ia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIRecommendationDTO
{
    private Long productId;
    private String productName;
    private String description;
    private Double price;
    private String producerName;
    private String category;
    private Double recommendationScore;
    private String reason;
}