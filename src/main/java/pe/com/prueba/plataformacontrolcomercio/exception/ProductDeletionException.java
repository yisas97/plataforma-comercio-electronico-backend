package pe.com.prueba.plataformacontrolcomercio.exception;

public class ProductDeletionException extends RuntimeException {

    private final String errorCode;
    private final boolean canMarkInactive;

    public ProductDeletionException(String message, String errorCode, boolean canMarkInactive) {
        super(message);
        this.errorCode = errorCode;
        this.canMarkInactive = canMarkInactive;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean canMarkInactive() {
        return canMarkInactive;
    }
}