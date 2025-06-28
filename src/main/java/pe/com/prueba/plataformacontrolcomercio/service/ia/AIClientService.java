package pe.com.prueba.plataformacontrolcomercio.service.ia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIClientService implements IAIClientService{

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public AIClientService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Async
    public void trackInteraction(Long userId, Long productId, String actionType) {
        try {
            Map<String, Object> request = Map.of(
                    "userId", userId,
                    "productId", productId,
                    "actionType", actionType
            );

            log.info("Sending interaction to AI service: userId={}, productId={}, actionType={}",
                    userId, productId, actionType);

            restTemplate.postForObject(
                    aiServiceUrl + "/api/ai/interactions",
                    request,
                    String.class
            );

            log.debug("Interaction tracked successfully");

        } catch (Exception e) {
            log.error("Error tracking interaction for user {} on product {}: {}",
                    userId, productId, e.getMessage());
        }
    }


    public List<Object> getRecommendations(Long userId, int limit) {
        try {
            String url = aiServiceUrl + "/api/ai/recommendations/" + userId + "?limit=" + limit;
            Object[] recommendations = restTemplate.getForObject(url, Object[].class);

            return recommendations != null ? Arrays.asList(recommendations) : List.of();

        } catch (Exception e) {
            log.error("Error getting recommendations for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    public List<Object> getPopularRecommendations(int limit) {
        try {
            String url = aiServiceUrl + "/api/ai/recommendations/popular?limit=" + limit;
            Object[] recommendations = restTemplate.getForObject(url, Object[].class);

            return recommendations != null ? Arrays.asList(recommendations) : List.of();

        } catch (Exception e) {
            log.error("Error getting popular recommendations: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean isAIServiceAvailable() {
        try {
            String healthUrl = aiServiceUrl + "/api/ai/health";
            String response = restTemplate.getForObject(healthUrl, String.class);
            return response != null && response.contains("running");

        } catch (Exception e) {
            log.warn("AI service not available: {}", e.getMessage());
            return false;
        }
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}