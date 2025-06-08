package pe.com.prueba.plataformacontrolcomercio.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.model.Product;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@Slf4j
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    private final Map<String, Duration> TTL_CONFIG = Map.of(
            "product", Duration.ofHours(1),
            "category", Duration.ofHours(2),
            "user", Duration.ofMinutes(30),
            "producer", Duration.ofMinutes(30),
            "order", Duration.ofMinutes(10),
            "cart", Duration.ofMinutes(5)
    );

    @Autowired
    public CacheService(RedisTemplate<String, String> redisTemplate,
            EntityManager entityManager) {
        this.redisTemplate = redisTemplate;
        this.entityManager = entityManager;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public <T> T getFromCache(String key, Class<T> type, Supplier<T> fallback) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                CompletableFuture.runAsync(() -> updateCacheMetricsAsync(key, true));

                T result = deserializeByType(cached, type, key);
                if (result != null) {
                    return result;
                }
            }

            T data = fallback.get();
            if (data != null) {
                setCache(key, data);
                CompletableFuture.runAsync(() -> updateCacheMetricsAsync(key, false));
            }
            return data;

        } catch (Exception e) {
            log.error("Error en caché para key: {}", key, e);
            return fallback.get();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeByType(String cached, Class<T> type, String key) {
        try {
            if (type == List.class) {
                if (key.contains("products") || key.contains("product")) {
                    JavaType listType = objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Product.class);
                    return (T) objectMapper.readValue(cached, listType);
                } else {
                    return (T) objectMapper.readValue(cached, new TypeReference<List<Object>>() {});
                }
            } else if (type == Product.class) {
                return (T) objectMapper.readValue(cached, Product.class);
            } else {
                return objectMapper.readValue(cached, type);
            }
        } catch (Exception e) {
            log.warn("Error deserializando caché para key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    public void setCache(String key, Object value) {
        try {
            String entityType = extractEntityType(key);
            Duration ttl = TTL_CONFIG.getOrDefault(entityType, Duration.ofHours(1));

            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl);

            updateCacheControlAsync(key, entityType, extractEntityId(key));

        } catch (Exception e) {
            log.error("Error al guardar en caché: {}", key, e);
        }
    }

    public void invalidateCache(String key) {
        try {
            redisTemplate.delete(key);
            deleteCacheControlAsync(key);
        } catch (Exception e) {
            log.error("Error al invalidar caché: {}", key, e);
        }
    }

    public void invalidatePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                keys.forEach(this::deleteCacheControlAsync);
                log.info("Invalidated {} keys with pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Error al invalidar patrón: {}", pattern, e);
        }
    }

    private void updateCacheMetricsAsync(String key, boolean hit) {
        try {
            entityManager.getTransaction().begin();

            String sql = hit ?
                    "UPDATE cache_metrics SET hit_count = hit_count + 1, last_accessed = NOW() WHERE cache_key = ?" :
                    "UPDATE cache_metrics SET miss_count = miss_count + 1, last_accessed = NOW() WHERE cache_key = ?";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, key);
            int updated = query.executeUpdate();

            if (updated == 0 && !hit) {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT IGNORE INTO cache_metrics (cache_key, hit_count, miss_count, last_accessed) VALUES (?, 0, 1, NOW())");
                insertQuery.setParameter(1, key);
                insertQuery.executeUpdate();
            }

            entityManager.getTransaction().commit();
            log.debug("Cache metrics updated for key: {}", key);

        } catch (Exception e) {
            try {
                entityManager.getTransaction().rollback();
            } catch (Exception rollbackEx) {
                log.warn("Error en rollback: {}", rollbackEx.getMessage());
            }
            log.warn("Error al actualizar métricas de caché: {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCacheControlAsync(String key, String entityType, Long entityId) {
        try {
            Query query = entityManager.createNativeQuery(
                    "INSERT INTO cache_control (cache_key, entity_type, entity_id, last_modified, version_number) " +
                            "VALUES (?, ?, ?, NOW(), 1) " +
                            "ON DUPLICATE KEY UPDATE last_modified = NOW(), version_number = version_number + 1");
            query.setParameter(1, key);
            query.setParameter(2, entityType);
            query.setParameter(3, entityId);
            query.executeUpdate();

            log.debug("Cache control updated for key: {}", key);
        } catch (Exception e) {
            log.warn("Error al actualizar control de caché: {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteCacheControlAsync(String key) {
        try {
            Query query = entityManager.createNativeQuery("DELETE FROM cache_control WHERE cache_key = ?");
            query.setParameter(1, key);
            query.executeUpdate();
        } catch (Exception e) {
            log.warn("Error al eliminar control de caché: {}", e.getMessage());
        }
    }

    private String extractEntityType(String key) {
        return key.split(":")[0];
    }

    private Long extractEntityId(String key) {
        try {
            String[] parts = key.split(":");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}