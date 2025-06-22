package pe.com.prueba.plataformacontrolcomercio.service.ia;

import org.springframework.web.client.RestTemplate;

import java.util.List;

public interface IAIClientService
{
    public void trackInteraction(Long userId, Long productId,
            String actionType);

    public List<Object> getRecommendations(Long userId, int limit);

    public List<Object> getPopularRecommendations(int limit);

    public boolean isAIServiceAvailable();

    public String getAiServiceUrl();

    public RestTemplate getRestTemplate();
}
