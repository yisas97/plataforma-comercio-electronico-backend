package pe.com.prueba.plataformacontrolcomercio.dto.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerMarketplaceDTO
{
    private Long id;
    private String businessName;
    private String description;
    private String location;
    private String phone;
    private Long productCount;
    private Double averageRating;

    public ProducerMarketplaceDTO(Long id, String businessName,
            String description, String location, String phone,
            Long productCount)
    {
        this.id = id;
        this.businessName = businessName;
        this.description = description;
        this.location = location;
        this.phone = phone;
        this.productCount = productCount;
        this.averageRating = 4.5;
    }
}