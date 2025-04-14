package pe.com.prueba.plataformacontrolcomercio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.prueba.plataformacontrolcomercio.model.Tag;
import pe.com.prueba.plataformacontrolcomercio.repository.TagRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TagService implements ITagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    @Override
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }

    @Override
    public List<Tag> searchTagsByName(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Tag createTag(Tag tag) {
        // Verificar si ya existe una etiqueta con el mismo nombre
        if (tagRepository.findByName(tag.getName()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una etiqueta con el nombre: " + tag.getName());
        }

        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        return tagRepository.save(tag);
    }

    @Override
    public Optional<Tag> updateTag(Long id, Tag tagDetails) {
        return tagRepository.findById(id).map(existingTag -> {
            // Verificar si el nuevo nombre ya existe en otra etiqueta
            if (!existingTag.getName().equals(tagDetails.getName()) &&
                    tagRepository.findByName(tagDetails.getName()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una etiqueta con el nombre: " + tagDetails.getName());
            }

            existingTag.setName(tagDetails.getName());
            existingTag.setUpdatedAt(LocalDateTime.now());
            return tagRepository.save(existingTag);
        });
    }

    @Override
    public boolean deleteTag(Long id) {
        return tagRepository.findById(id).map(tag -> {
            tagRepository.delete(tag);
            return true;
        }).orElse(false);
    }
}