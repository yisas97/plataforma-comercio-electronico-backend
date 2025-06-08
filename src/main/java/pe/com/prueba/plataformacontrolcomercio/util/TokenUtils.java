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
public class TokenUtils
{

    private final SecretKey key;
    private final UserRepository userRepository;
    private final ProducerRepository producerRepository;

    @Autowired
    public TokenUtils(@Value("${app.jwt.secret}") String jwtSecret,
            UserRepository userRepository,
            ProducerRepository producerRepository)
    {

        this.key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.userRepository = userRepository;
        this.producerRepository = producerRepository;
    }

    /**
     * Obtiene el ID del productor a partir del token en la solicitud HTTP
     *
     * @param request
     *         La solicitud HTTP que contiene el token de autenticación
     * @return El ID del productor o null si no se pudo obtener o el usuario no
     *         es productor
     */
    public Long getProducerIdFromRequest(HttpServletRequest request)
    {
        try
        {

            String token = extractTokenFromRequest(request);
            if (token == null)
            {
                return null;
            }

            String userEmail = getUserEmailFromToken(token);
            if (userEmail == null)
            {
                return null;
            }

            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty())
            {
                return null;
            }

            User user = userOpt.get();

            if (!"ROLE_PRODUCER".equals(user.getRole().toString()))
            {
                return null;
            }

            Optional<Producer> producerOpt = producerRepository.findByUserId(
                    user.getId());
            return producerOpt.map(Producer::getId).orElse(null);

        } catch (Exception e)
        {

            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene el ID del usuario a partir del token en la solicitud HTTP
     *
     * @param request
     *         La solicitud HTTP que contiene el token de autenticación
     * @return El ID del usuario o null si no se pudo obtener
     */
    public Long getUserIdFromRequest(HttpServletRequest request)
    {
        try
        {

            String token = extractTokenFromRequest(request);
            if (token == null)
            {
                return null;
            }

            String userEmail = getUserEmailFromToken(token);
            if (userEmail == null)
            {
                return null;
            }

            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            return userOpt.map(User::getId).orElse(null);

        } catch (Exception e)
        {

            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene el rol del usuario a partir del token en la solicitud HTTP
     *
     * @param request
     *         La solicitud HTTP que contiene el token de autenticación
     * @return El rol del usuario o null si no se pudo obtener
     */
    public String getRoleFromRequest(HttpServletRequest request)
    {
        try
        {

            String token = extractTokenFromRequest(request);
            if (token == null)
            {
                return null;
            }

            String userEmail = getUserEmailFromToken(token);
            if (userEmail == null)
            {
                return null;
            }

            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            return userOpt.map(user -> user.getRole().toString()).orElse(null);

        } catch (Exception e)
        {

            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrae el token de la solicitud HTTP
     *
     * @param request
     *         La solicitud HTTP
     * @return El token extraído o null si no se encontró
     */
    private String extractTokenFromRequest(HttpServletRequest request)
    {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            return authHeader.substring(7);
        }

        return null;
    }

    /**
     * Extrae el email del usuario del token JWT
     *
     * @param token
     *         El token JWT
     * @return El email del usuario o null si no se pudo extraer
     */
    private String getUserEmailFromToken(String token)
    {
        try
        {

            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();

            return claims.getSubject();
        } catch (Exception e)
        {

            e.printStackTrace();
            return null;
        }
    }
}