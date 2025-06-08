package pe.com.prueba.plataformacontrolcomercio.mapper;

import org.springframework.stereotype.Component;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderDTO;
import pe.com.prueba.plataformacontrolcomercio.dto.order.OrderItemDTO;
import pe.com.prueba.plataformacontrolcomercio.model.Order;
import pe.com.prueba.plataformacontrolcomercio.model.OrderItem;

import java.util.stream.Collectors;

@Component
public class OrderMapper
{

    public OrderDTO toDTO(Order order)
    {
        if (order == null)
        {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getUser() != null)
        {
            dto.setUserId(order.getUser().getId());
            dto.setUserName(order.getUser().getName());
            dto.setUserEmail(order.getUser().getEmail());
        }

        if (order.getOrderItems() != null)
        {
            dto.setOrderItems(
                    order.getOrderItems().stream().map(this::toOrderItemDTO)
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    public OrderItemDTO toOrderItemDTO(OrderItem orderItem)
    {
        if (orderItem == null)
        {
            return null;
        }

        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());

        if (orderItem.getProduct() != null)
        {
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getName());
            // Si tienes campo image en Product, descomenta la siguiente línea:
            // dto.setProductImage(orderItem.getProduct().getImage());
        }

        return dto;
    }

    public Order toEntity(OrderDTO dto)
    {
        if (dto == null)
        {
            return null;
        }

        Order order = new Order();
        order.setId(dto.getId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setStatus(dto.getStatus());
        order.setShippingAddress(dto.getShippingAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setNotes(dto.getNotes());
        order.setCreatedAt(dto.getCreatedAt());
        order.setUpdatedAt(dto.getUpdatedAt());

        // Nota: User y OrderItems deben ser establecidos por el servicio
        // ya que requieren consultas a la base de datos

        return order;
    }
}