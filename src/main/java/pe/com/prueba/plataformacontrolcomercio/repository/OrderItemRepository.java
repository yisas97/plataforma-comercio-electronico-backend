package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>
{

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);
}
