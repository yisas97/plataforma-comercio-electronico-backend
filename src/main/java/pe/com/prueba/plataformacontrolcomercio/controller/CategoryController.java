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
import pe.com.prueba.plataformacontrolcomercio.model.Category;
import pe.com.prueba.plataformacontrolcomercio.service.ICategoryService;
import pe.com.prueba.plataformacontrolcomercio.util.TokenUtils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Slf4j
public class CategoryController {

    private final ICategoryService categoryService;
    private final TokenUtils tokenUtils;

    @Autowired
    public CategoryController(ICategoryService categoryService, TokenUtils tokenUtils) {
        this.categoryService = categoryService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(HttpServletRequest request) {
        if (tokenUtils.getUserIdFromRequest(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        if (tokenUtils.getUserIdFromRequest(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String name, HttpServletRequest request) {
        if (tokenUtils.getUserIdFromRequest(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(categoryService.searchCategoriesByName(name));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category, HttpServletRequest request) {
        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Category newCategory = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category,
            HttpServletRequest request) {

        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return categoryService.updateCategory(id, category)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, HttpServletRequest request) {

        String role = tokenUtils.getRoleFromRequest(request);
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return categoryService.deleteCategory(id)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}