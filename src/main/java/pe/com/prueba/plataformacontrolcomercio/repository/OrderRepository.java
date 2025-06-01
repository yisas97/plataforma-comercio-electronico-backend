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

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi " +
            "WHERE oi.product.producer.id = :producerId AND o.status = :status " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByProducerIdAndStatusOrderByCreatedAtDesc(@Param("producerId") Long producerId,
            @Param("status") OrderStatus status);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi " +
            "WHERE oi.product.producer.id = :producerId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByProducerIdAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("producerId") Long producerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems oi " +
            "WHERE oi.product.producer.id = :producerId AND o.status = :status")
    Long countByProducerIdAndStatus(@Param("producerId") Long producerId, @Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o JOIN o.orderItems oi " +
            "WHERE oi.product.producer.id = :producerId AND o.status != 'CANCELLED'")
    Double getTotalRevenueByProducerId(@Param("producerId") Long producerId);

    @Query("SELECT oi.product.name, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "WHERE oi.product.producer.id = :producerId " +
            "AND oi.order.status != 'CANCELLED' " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProductByProducerId(@Param("producerId") Long producerId);
}