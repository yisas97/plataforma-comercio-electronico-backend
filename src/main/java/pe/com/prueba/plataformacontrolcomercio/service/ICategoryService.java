package pe.com.prueba.plataformacontrolcomercio.service;

import pe.com.prueba.plataformacontrolcomercio.model.Category;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Optional<Category> getCategoryByName(String name);
    List<Category> searchCategoriesByName(String name);
    Category createCategory(Category category);
    Optional<Category> updateCategory(Long id, Category category);
    boolean deleteCategory(Long id);
}