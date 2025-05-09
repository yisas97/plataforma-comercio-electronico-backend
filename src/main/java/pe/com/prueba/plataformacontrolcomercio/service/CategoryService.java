package pe.com.prueba.plataformacontrolcomercio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.prueba.plataformacontrolcomercio.model.Category;
import pe.com.prueba.plataformacontrolcomercio.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + category.getName());
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    public Optional<Category> updateCategory(Long id, Category categoryDetails) {
        return categoryRepository.findById(id).map(existingCategory -> {
            // Verificar si el nuevo nombre ya existe en otra categoría
            if (!existingCategory.getName().equals(categoryDetails.getName()) &&
                    categoryRepository.findByName(categoryDetails.getName()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + categoryDetails.getName());
            }
            existingCategory.setActive(categoryDetails.getActive());
            existingCategory.setName(categoryDetails.getName());
            existingCategory.setDescription(categoryDetails.getDescription());
            existingCategory.setUpdatedAt(LocalDateTime.now());
            return categoryRepository.save(existingCategory);
        });
    }

    @Override
    public boolean deleteCategory(Long id) {
        return categoryRepository.findById(id).map(category -> {
            categoryRepository.delete(category);
            return true;
        }).orElse(false);
    }
}