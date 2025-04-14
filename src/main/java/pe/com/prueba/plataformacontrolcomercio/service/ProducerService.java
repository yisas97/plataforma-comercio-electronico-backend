package pe.com.prueba.plataformacontrolcomercio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.prueba.plataformacontrolcomercio.model.Producer;
import pe.com.prueba.plataformacontrolcomercio.repository.ProducerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProducerService implements IProducerService
{

    private final ProducerRepository producerRepository;

    @Autowired
    public ProducerService(ProducerRepository producerRepository)
    {
        this.producerRepository = producerRepository;
    }

    public List<Producer> getAllProducers()
    {
        return producerRepository.findAll();
    }

    public Optional<Producer> getProducerById(Long id)
    {
        return producerRepository.findById(id);
    }

    public Optional<Producer> findByUserId(Long userId)
    {
        return producerRepository.findByUserId(userId);
    }

    public Producer saveProducer(Producer producer)
    {
        return producerRepository.save(producer);
    }

    public void deleteProducer(Long id)
    {
        producerRepository.deleteById(id);
    }
}