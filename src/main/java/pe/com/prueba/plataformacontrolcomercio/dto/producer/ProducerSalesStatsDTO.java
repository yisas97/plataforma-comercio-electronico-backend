package pe.com.prueba.plataformacontrolcomercio.dto.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerSalesStatsDTO
{

    private Long totalOrders;
    private Double totalRevenue;
    private Double averageOrderValue;

    private Long pendingOrders;
    private Long confirmedOrders;
    private Long preparingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    private Long todayOrders;
    private Double todayRevenue;
    private Long thisWeekOrders;
    private Double thisWeekRevenue;
    private Long thisMonthOrders;
    private Double thisMonthRevenue;

    private String bestSellingProduct;
    private Long bestSellingProductQuantity;

    private LocalDateTime lastOrderDate;
    private LocalDateTime statsGeneratedAt;

    public ProducerSalesStatsDTO(Long totalOrders, Double totalRevenue,
            Long pendingOrders, Long confirmedOrders, Long preparingOrders,
            Long shippedOrders, Long deliveredOrders, Long cancelledOrders)
    {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        this.pendingOrders = pendingOrders;
        this.confirmedOrders = confirmedOrders;
        this.preparingOrders = preparingOrders;
        this.shippedOrders = shippedOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.averageOrderValue = totalOrders > 0 ?
                this.totalRevenue / totalOrders :
                0.0;
        this.statsGeneratedAt = LocalDateTime.now();
    }
}