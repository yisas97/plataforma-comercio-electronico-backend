package pe.com.prueba.plataformacontrolcomercio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.prueba.plataformacontrolcomercio.model.Order;
import pe.com.prueba.plataformacontrolcomercio.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>
{

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE oi.product.producer.id = :producerId ORDER BY o.createdAt DESC")
    List<Order> findByProducerIdOrderByCreatedAtDesc(@Param("producerId") Long producerId);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findByDateRangeOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countOrdersByUserId(@Param("userId") Long userId);
}