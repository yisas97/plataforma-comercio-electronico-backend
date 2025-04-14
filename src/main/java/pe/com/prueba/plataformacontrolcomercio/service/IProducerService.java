package pe.com.prueba.plataformacontrolcomercio.service;

import pe.com.prueba.plataformacontrolcomercio.model.Producer;

import java.util.List;
import java.util.Optional;

public interface IProducerService
{
    public List<Producer> getAllProducers();

    public Optional<Producer> getProducerById(Long id);
    public Optional<Producer> findByUserId(Long userId);

    public Producer saveProducer(Producer producer);

    public void deleteProducer(Long id);
}
