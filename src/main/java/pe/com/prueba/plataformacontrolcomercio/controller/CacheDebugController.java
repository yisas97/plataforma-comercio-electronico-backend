package pe.com.prueba.plataformacontrolcomercio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.prueba.plataformacontrolcomercio.service.cache.CacheService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cache-debug")
@Slf4j
public class CacheDebugController
{

    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private final CacheService cacheService;

    public CacheDebugController(CacheService cacheService)
    {
        this.cacheService = cacheService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus()
    {
        Map<String, Object> status = new HashMap<>();

        try
        {
            boolean isAvailable = cacheService.isRedisAvailable();
            Long cacheSize = cacheService.getCacheSize();
            Set<String> allKeys = cacheService.getAllKeys();

            status.put("redisAvailable", isAvailable);
            status.put("totalKeys", cacheSize);
            status.put("keys", allKeys);
            status.put(STATUS, isAvailable ? "FUNCIONANDO" : "NO DISPONIBLE");

            log.info("Cache status: available={}, keys={}", isAvailable,
                    cacheSize);

        } catch (Exception e)
        {
            status.put(ERROR, e.getMessage());
            status.put(STATUS, "ERROR");
            log.error("Error checking cache status", e);
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<Map<String, Object>> getCacheKey(
            @PathVariable String key)
    {
        Map<String, Object> result = new HashMap<>();

        try
        {
            String value = cacheService.getCacheValue(key);
            result.put("key", key);
            result.put("exists", value != null);
            result.put("value", value != null ?
                    value.substring(0, Math.min(500, value.length())) + "..." :
                    null);
            result.put("valueLength", value != null ? value.length() : 0);

        } catch (Exception e)
        {
            result.put(ERROR, e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllCache()
    {
        Map<String, Object> result = new HashMap<>();

        try
        {
            Set<String> keys = cacheService.getAllKeys();
            int keyCount = keys.size();

            cacheService.invalidatePattern("*");

            result.put("message", "Cache cleared");
            result.put("keysDeleted", keyCount);
            result.put(STATUS, "SUCCESS");

            log.info("Cache cleared: {} keys deleted", keyCount);

        } catch (Exception e)
        {
            result.put(ERROR, e.getMessage());
            result.put(STATUS, "ERROR");
        }

        return ResponseEntity.ok(result);
    }
}