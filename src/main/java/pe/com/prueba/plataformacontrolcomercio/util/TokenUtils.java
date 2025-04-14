package pe.com.prueba.plataformacontrolcomercio.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;
import pe.com.prueba.plataformacontrolcomercio.model.User;
import pe.com.prueba.plataformacontrolcomercio.repository.ProducerRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class TokenUtils {

    private final SecretKey key;
    private final UserRepository userRepository;
    private final ProducerRepository producerRepository;

    @Autowired
    public TokenUtils(@Value("${app.jwt.secret}") String jwtSecret,
            UserRepository userRepository,
            ProducerRepository producerRepository) {
        // Convertir la clave secreta String a una SecretKey
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.userRepository = userRepository;
        this.producerRepository = producerRepository;
    }

    /**
     * Obtiene el ID del productor a partir del token en la solicitud HTTP
     * @param request La solicitud HTTP que contiene el token de autenticación
     * @return El ID del productor o null si no se pudo obtener o el usuario no es productor
     */
    public Long getProducerIdFromRequest(HttpServletRequest request) {
        try {
            // 1. Obtener el email del usuario del token
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return null;
            }

            String userEmail = getUserEmailFromToken(token);
            if (userEmail == null) {
                return null;
            }

            // 2. Buscar el usuario por email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return null;
            }

            User user = userOpt.get();

            // 3. Verificar si el usuario es un productor
            if (!"ROLE_PRODUCER".equals(user.getRole().toString())) {
                return null;
            }

            // 4. Buscar el productor asociado al usuario
            Optional<Producer> producerOpt = producerRepository.findByUserId(user.getId());
            return producerOpt.map(Producer::getId).orElse(null);

        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene el ID del usuario a partir del token en la solicitud HTTP
     * @param request La solicitud HTTP que contiene el token de autenticación
     * @return El ID del usuario o null si no se pudo obtener
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            // 1. Obtener el email del usuario del token
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return null;
            }

            String userEmail = getUserEmailFromToken(token);
            if (userEmail == null) {
                return null;
            }

            // 2. Buscar el usuario por email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            return userOpt.map(User::getId).orElse(null);

        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene el rol del usuario a partir del token en la solicitud HTTP
     * @param request La solicitud HTTP que contiene el token de autenticación
     * @return El rol del usuario o null si no se pudo obtener
     */
    public String getRoleFromRequest(HttpServletRequest request) {
        try {
            // 1. Obtener el email del usuario del token
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return null;
            }

            String userEmail = getUserEmailFromToken(token);
            if (userEmail == null) {
                return null;
            }

            // 2. Buscar el usuario por email y obtener su rol
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            return userOpt.map(user -> user.getRole().toString()).orElse(null);

        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrae el token de la solicitud HTTP
     * @param request La solicitud HTTP
     * @return El token extraído o null si no se encontró
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Obtener el encabezado de autorización
        String authHeader = request.getHeader("Authorization");

        // Verificar si existe y tiene el formato correcto (Bearer token)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Eliminar "Bearer " del inicio
        }

        return null;
    }

    /**
     * Extrae el email del usuario del token JWT
     * @param token El token JWT
     * @return El email del usuario o null si no se pudo extraer
     */
    private String getUserEmailFromToken(String token) {
        try {
            // Nueva sintaxis de JJWT 0.11.x
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // El "sub" en tu token es el email del usuario
            return claims.getSubject();
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return null;
        }
    }
}