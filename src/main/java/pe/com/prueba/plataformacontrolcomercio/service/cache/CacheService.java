package pe.com.prueba.plataformacontrolcomercio.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Slf4j
public class CacheService
{

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, Duration> TTL_CONFIG = Map.of("product",
            Duration.ofHours(1), "category", Duration.ofHours(2), "user",
            Duration.ofMinutes(30), "producer", Duration.ofMinutes(30), "order",
            Duration.ofMinutes(10), "cart", Duration.ofMinutes(5));

    @Autowired
    public CacheService(RedisTemplate<String, String> redisTemplate)
    {
        this.redisTemplate = redisTemplate;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
    }

    public <T> T getFromCache(String key, Class<T> type, Supplier<T> fallback)
    {
        try
        {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty())
            {
                log.info("CACHE HIT para key: {}", key);

                T result = deserialize(cached, type);
                if (result != null)
                {
                    return result;
                }
            }

            log.info("CACHE MISS para key: {} - Obteniendo de BD", key);
            T data = fallback.get();
            if (data != null)
            {
                setCache(key, data);
                log.info("Datos guardados en caché para key: {}", key);
            }
            return data;

        } catch (Exception e)
        {
            log.error("Error en caché para key: {}", key, e);
            return fallback.get();
        }
    }

    public <T> T getFromCache(String key, TypeReference<T> typeReference,
            Supplier<T> fallback)
    {
        try
        {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty())
            {
                log.info("CACHE HIT para key: {}", key);

                T result = deserialize(cached, typeReference);
                if (result != null)
                {
                    return result;
                }
            }

            log.info("CACHE MISS para key: {} - Obteniendo de BD", key);
            T data = fallback.get();
            if (data != null)
            {
                setCache(key, data);
                log.info("Datos guardados en caché para key: {}", key);
            }
            return data;

        } catch (Exception e)
        {
            log.error("Error en caché para key: {}", key, e);
            return fallback.get();
        }
    }

    public <T> T getFromCache(String key, JavaType javaType,
            Supplier<T> fallback)
    {
        try
        {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty())
            {
                log.info("CACHE HIT para key: {}", key);

                T result = deserialize(cached, javaType);
                if (result != null)
                {
                    return result;
                }
            }

            log.info("CACHE MISS para key: {} - Obteniendo de BD", key);
            T data = fallback.get();
            if (data != null)
            {
                setCache(key, data);
                log.info("Datos guardados en caché para key: {}", key);
            }
            return data;

        } catch (Exception e)
        {
            log.error("Error en caché para key: {}", key, e);
            return fallback.get();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(String cached, Class<T> type)
    {
        try
        {
            if (type == String.class)
            {
                return (T) cached;
            } else if (type == Long.class)
            {
                return (T) Long.valueOf(cached);
            } else if (type == Double.class)
            {
                return (T) Double.valueOf(cached);
            } else if (type == Integer.class)
            {
                return (T) Integer.valueOf(cached);
            } else if (type == Boolean.class)
            {
                return (T) Boolean.valueOf(cached);
            } else
            {
                return objectMapper.readValue(cached, type);
            }
        } catch (Exception e)
        {
            log.warn("⚠️ Error deserializando caché para tipo: {}, error: {}",
                    type.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private <T> T deserialize(String cached, TypeReference<T> typeReference)
    {
        try
        {
            return objectMapper.readValue(cached, typeReference);
        } catch (Exception e)
        {
            log.warn(
                    "⚠️ Error deserializando caché con TypeReference, error: {}",
                    e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(String cached, JavaType javaType)
    {
        try
        {
            return (T) objectMapper.readValue(cached, javaType);
        } catch (Exception e)
        {
            log.warn("⚠️ Error deserializando caché con JavaType, error: {}",
                    e.getMessage());
            return null;
        }
    }

    public void setCache(String key, Object value)
    {
        try
        {
            String entityType = extractEntityType(key);
            Duration ttl = TTL_CONFIG.getOrDefault(entityType,
                    Duration.ofHours(1));

            String jsonValue;
            if (value instanceof String || value instanceof Number || value instanceof Boolean)
            {
                jsonValue = String.valueOf(value);
            } else
            {
                jsonValue = objectMapper.writeValueAsString(value);
            }

            redisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("Cache guardado: key={}, ttl={}", key, ttl);

        } catch (Exception e)
        {
            log.error("Error al guardar en caché: {}", key, e);
        }
    }

    public void invalidateCache(String key)
    {
        try
        {
            Boolean deleted = redisTemplate.delete(key);
            log.info("🗑Cache invalidado: key={}, deleted={}", key, deleted);
        } catch (Exception e)
        {
            log.error("Error al invalidar caché: {}", key, e);
        }
    }

    public void invalidatePattern(String pattern)
    {
        try
        {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty())
            {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("🗑Invalidated {} keys with pattern: {}", deletedCount,
                        pattern);
            } else
            {
                log.info("No keys found for pattern: {}", pattern);
            }
        } catch (Exception e)
        {
            log.error("Error al invalidar patrón: {}", pattern, e);
        }
    }

    public boolean isRedisAvailable()
    {
        try
        {
            redisTemplate.opsForValue()
                    .set("test:connection", "OK", Duration.ofSeconds(10));
            String result = redisTemplate.opsForValue().get("test:connection");
            redisTemplate.delete("test:connection");
            return "OK".equals(result);
        } catch (Exception e)
        {
            log.error("Redis no disponible: {}", e.getMessage());
            return false;
        }
    }

    public Set<String> getAllKeys()
    {
        return redisTemplate.keys("*");
    }

    public String getCacheValue(String key)
    {
        return redisTemplate.opsForValue().get(key);
    }

    public Long getCacheSize()
    {
        Set<String> keys = redisTemplate.keys("*");
        return keys != null ? (long) keys.size() : 0L;
    }

    private String extractEntityType(String key)
    {
        return key.split(":")[0];
    }

    public JavaType listType(Class<?> elementClass)
    {
        return objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementClass);
    }

    public JavaType mapType(Class<?> keyClass, Class<?> valueClass)
    {
        return objectMapper.getTypeFactory()
                .constructMapType(Map.class, keyClass, valueClass);
    }
}