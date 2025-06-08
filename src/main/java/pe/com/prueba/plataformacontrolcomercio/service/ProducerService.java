package pe.com.prueba.plataformacontrolcomercio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;
import pe.com.prueba.plataformacontrolcomercio.repository.ProducerRepository;
import pe.com.prueba.plataformacontrolcomercio.service.cache.CacheService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProducerService implements IProducerService
{

    private final ProducerRepository producerRepository;
    private final CacheService cacheService;

    @Autowired
    public ProducerService(ProducerRepository producerRepository,
            CacheService cacheService)
    {
        this.producerRepository = producerRepository;
        this.cacheService = cacheService;
    }

    public List<Producer> getAllProducers()
    {
        String cacheKey = "producers:all";
        return cacheService.getFromCache(cacheKey,
                new TypeReference<List<Producer>>()
                {
                }, () -> {
                    log.info("Fetching all producers from database");
                    return producerRepository.findAll();
                });
    }

    public Optional<Producer> getProducerById(Long id)
    {
        String cacheKey = "producer:" + id;
        Producer cachedProducer = cacheService.getFromCache(cacheKey,
                Producer.class, () -> {
                    log.info("Fetching producer {} from database", id);
                    return producerRepository.findById(id).orElse(null);
                });
        return Optional.ofNullable(cachedProducer);
    }

    public Optional<Producer> findByUserId(Long userId)
    {
        String cacheKey = "producer:user:" + userId;
        Producer cachedProducer = cacheService.getFromCache(cacheKey,
                Producer.class, () -> {
                    log.info("Fetching producer by user {} from database",
                            userId);
                    return producerRepository.findByUserId(userId).orElse(null);
                });
        return Optional.ofNullable(cachedProducer);
    }

    @Transactional
    public Producer saveProducer(Producer producer)
    {
        log.info("Saving producer: {}", producer.getBusinessName());

        Producer savedProducer = producerRepository.save(producer);

        cacheService.invalidatePattern("producers:*");
        cacheService.invalidateCache("producer:" + savedProducer.getId());

        if (savedProducer.getUser() != null && savedProducer.getUser()
                .getId() != null)
        {
            cacheService.invalidateCache(
                    "producer:user:" + savedProducer.getUser().getId());
        }

        cacheService.invalidatePattern("products:marketplace:*");

        log.info("Cache invalidated after saving producer: {}",
                savedProducer.getId());
        return savedProducer;
    }

    @Transactional
    public void deleteProducer(Long id)
    {
        log.info("Deleting producer: {}", id);

        Optional<Producer> producerOpt = getProducerById(id);

        producerRepository.deleteById(id);

        cacheService.invalidatePattern("producers:*");
        cacheService.invalidateCache("producer:" + id);

        if (producerOpt.isPresent() && producerOpt.get()
                .getUser() != null && producerOpt.get().getUser()
                .getId() != null)
        {
            cacheService.invalidateCache(
                    "producer:user:" + producerOpt.get().getUser().getId());
        }

        cacheService.invalidatePattern("products:producer:" + id);
        cacheService.invalidatePattern("products:marketplace:producer:" + id);
        cacheService.invalidatePattern("orders:producer:" + id);
        cacheService.invalidatePattern("stats:producer:" + id);

        log.info("Cache invalidated after deleting producer: {}", id);
    }

    public List<Producer> getApprovedProducers()
    {
        String cacheKey = "producers:approved";
        return cacheService.getFromCache(cacheKey,
                new TypeReference<List<Producer>>()
                {
                }, () -> {
                    log.info("Fetching approved producers from database");
                    return producerRepository.findByApprovedTrue();
                });
    }

    public List<Producer> getProducersByLocation(String location)
    {
        String cacheKey = "producers:location:" + location.toLowerCase();
        return cacheService.getFromCache(cacheKey,
                new TypeReference<List<Producer>>()
                {
                }, () -> {
                    log.info("Fetching producers by location {} from database",
                            location);
                    return producerRepository.findByLocationContainingIgnoreCase(
                            location);
                });
    }

    @Transactional
    public Producer approveProducer(Long producerId)
    {
        log.info("Approving producer: {}", producerId);

        Producer producer = producerRepository.findById(producerId).orElseThrow(
                () -> new IllegalArgumentException("Productor no encontrado"));

        producer.setApproved(true);
        Producer savedProducer = producerRepository.save(producer);

        cacheService.invalidatePattern("producers:*");
        cacheService.invalidatePattern("products:marketplace:*");
        cacheService.invalidateCache("producer:" + producerId);

        if (producer.getUser() != null && producer.getUser().getId() != null)
        {
            cacheService.invalidateCache(
                    "producer:user:" + producer.getUser().getId());
        }

        log.info("Cache invalidated after approving producer: {}", producerId);
        return savedProducer;
    }

    @Transactional
    public Producer rejectProducer(Long producerId)
    {
        log.info("Rejecting producer: {}", producerId);

        Producer producer = producerRepository.findById(producerId).orElseThrow(
                () -> new IllegalArgumentException("Productor no encontrado"));

        producer.setApproved(false);
        Producer savedProducer = producerRepository.save(producer);

        cacheService.invalidatePattern("producers:*");
        cacheService.invalidatePattern("products:marketplace:*");
        cacheService.invalidateCache("producer:" + producerId);

        if (producer.getUser() != null && producer.getUser().getId() != null)
        {
            cacheService.invalidateCache(
                    "producer:user:" + producer.getUser().getId());
        }

        log.info("Cache invalidated after rejecting producer: {}", producerId);
        return savedProducer;
    }

    public Long getProducerCount()
    {
        String cacheKey = "producers:count";
        return cacheService.getFromCache(cacheKey, Long.class, () -> {
            log.info("Counting producers from database");
            return producerRepository.count();
        });
    }

    public Long getApprovedProducerCount()
    {
        String cacheKey = "producers:approved:count";
        return cacheService.getFromCache(cacheKey, Long.class, () -> {
            log.info("Counting approved producers from database");
            return producerRepository.countByApprovedTrue();
        });
    }

    public List<Producer> getPendingApprovalProducers()
    {
        String cacheKey = "producers:pending";
        return cacheService.getFromCache(cacheKey,
                new TypeReference<List<Producer>>()
                {
                }, () -> {
                    log.info(
                            "Fetching pending approval producers from database");
                    return producerRepository.findPendingApproval();
                });
    }

    public List<Producer> searchProducersByBusinessName(String businessName)
    {
        String cacheKey = "producers:search:business:" + businessName.toLowerCase();
        return cacheService.getFromCache(cacheKey,
                new TypeReference<List<Producer>>()
                {
                }, () -> {
                    log.info("Searching producers by business name: {}",
                            businessName);
                    return producerRepository.findApprovedByBusinessNameContaining(
                            businessName);
                });
    }

    public List<Producer> getApprovedProducersWithStock()
    {
        String cacheKey = "producers:approved:with-stock";
        return cacheService.getFromCache(cacheKey,
                new TypeReference<List<Producer>>()
                {
                }, () -> {
                    log.info(
                            "Fetching approved producers with stock from database");
                    return producerRepository.findApprovedWithStock();
                });
    }
}