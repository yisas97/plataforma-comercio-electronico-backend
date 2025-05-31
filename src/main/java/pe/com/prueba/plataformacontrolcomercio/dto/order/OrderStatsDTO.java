package pe.com.prueba.plataformacontrolcomercio.dto.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderStatsDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Double totalSpent;
    private Double averageOrderValue;

    public OrderStatsDTO(Long totalOrders, Long pendingOrders, Long completedOrders,
            Long cancelledOrders, Double totalSpent) {
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.completedOrders = completedOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalSpent = totalSpent != null ? totalSpent : 0.0;
        this.averageOrderValue = totalOrders > 0 ? this.totalSpent / totalOrders : 0.0;
    }
}