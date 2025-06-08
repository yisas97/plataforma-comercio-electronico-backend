package pe.com.prueba.plataformacontrolcomercio.service;

import pe.com.prueba.plataformacontrolcomercio.model.Tag;

import java.util.List;
import java.util.Optional;

public interface ITagService
{
    List<Tag> getAllTags();

    Optional<Tag> getTagById(Long id);

    Optional<Tag> getTagByName(String name);

    List<Tag> searchTagsByName(String name);

    Tag createTag(Tag tag);

    Optional<Tag> updateTag(Long id, Tag tag);

    boolean deleteTag(Long id);
}