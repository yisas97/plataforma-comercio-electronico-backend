package pe.com.prueba.plataformacontrolcomercio.util;

import org.springframework.stereotype.Component;

@Component
public class BlockchainUtils
{
    /**
     * Convertir bytes a hexadecimal
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
