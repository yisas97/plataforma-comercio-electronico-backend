package pe.com.prueba.plataformacontrolcomercio.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.prueba.plataformacontrolcomercio.model.Tag;
import pe.com.prueba.plataformacontrolcomercio.service.ITagService;
import pe.com.prueba.plataformacontrolcomercio.util.TokenUtils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
@Slf4j
public class TagController
{

    private final ITagService tagService;
    private final TokenUtils tokenUtils;

    @Autowired
    public TagController(ITagService tagService, TokenUtils tokenUtils)
    {
        this.tagService = tagService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags(HttpServletRequest request)
    {
        if (tokenUtils.getUserIdFromRequest(request) == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id,
            HttpServletRequest request)
    {
        if (tokenUtils.getUserIdFromRequest(request) == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Tag> tag = tagService.getTagById(id);
        return tag.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam String name,
            HttpServletRequest request)
    {
        if (tokenUtils.getUserIdFromRequest(request) == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(tagService.searchTagsByName(name));
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@Valid @RequestBody Tag tag,
            HttpServletRequest request)
    {
        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            Tag newTag = tagService.createTag(tag);
            return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable Long id,
            @Valid @RequestBody Tag tag, HttpServletRequest request)
    {

        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            return tagService.updateTag(id, tag).map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id,
            HttpServletRequest request)
    {
        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try
        {
            return tagService.deleteTag(id) ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }
}