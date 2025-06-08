package pe.com.prueba.plataformacontrolcomercio.model;

public enum OrderStatus
{
    PENDING("Pendiente"), CONFIRMED("Confirmado"), PREPARING(
        "Preparando"), SHIPPED("Enviado"), DELIVERED("Entregado"), CANCELLED(
        "Cancelado");

    private final String displayName;

    OrderStatus(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}