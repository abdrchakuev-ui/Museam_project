package kz.enu.museum.service;

import kz.enu.museum.model.Category;
import kz.enu.museum.repository.CategoryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления категориями экспонатов.
 * Предоставляет методы для работы с иерархией категорий.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class CategoryService {
    
    private static final Logger logger = LogManager.getLogger(CategoryService.class);
    private final CategoryRepository repository;
    
    /**
     * Конструктор сервиса.
     *
     * @param repository репозиторий для работы с категориями
     */
    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Добавляет новую категорию.
     *
     * @param category категория для добавления
     * @return добавленная категория с ID
     */
    public Category addCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Название категории обязательно");
        }
        
        Category saved = repository.save(category);
        logger.info("Категория добавлена: " + category.getName());
        return saved;
    }
    
    /**
     * Обновляет категорию.
     *
     * @param category обновлённая категория
     * @return обновлённая категория
     */
    public Category updateCategory(Category category) {
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("Категория и её ID обязательны");
        }
        
        Category updated = repository.save(category);
        logger.info("Категория обновлена: " + category.getName());
        return updated;
    }
    
    /**
     * Удаляет категорию по ID.
     *
     * @param categoryId ID категории
     */
    public void deleteCategory(Long categoryId) {
        if (repository.deleteById(categoryId)) {
            logger.info("Категория удалена (ID: " + categoryId + ")");
        }
    }
    
    /**
     * Получает категорию по ID.
     *
     * @param categoryId ID категории
     * @return Optional с категорией
     */
    public Optional<Category> getCategory(Long categoryId) {
        return repository.findById(categoryId);
    }
    
    /**
     * Получает все категории.
     *
     * @return список всех категорий
     */
    public List<Category> getAllCategories() {
        return repository.findAll();
    }
    
    /**
     * Получает корневые категории (без родителя).
     *
     * @return список корневых категорий
     */
    public List<Category> getRootCategories() {
        return repository.findRootCategories();
    }
    
    /**
     * Получает подкатегории.
     *
     * @param parentId ID родительской категории
     * @return список подкатегорий
     */
    public List<Category> getSubcategories(Long parentId) {
        return repository.findSubcategories(parentId);
    }
    
    /**
     * Добавляет подкатегорию к существующей категории.
     *
     * @param parentId ID родительской категории
     * @param subcategory подкатегория
     */
    public void addSubcategory(Long parentId, Category subcategory) {
        Optional<Category> parent = repository.findById(parentId);
        if (parent.isPresent()) {
            parent.get().addSubcategory(subcategory);
            repository.save(parent.get());
            logger.info("Подкатегория добавлена: " + subcategory.getName());
        }
    }
    
    /**
     * Поиск категории по коду.
     *
     * @param code код категории
     * @return Optional с найденной категорией
     */
    public Optional<Category> findByCode(String code) {
        return repository.findByCode(code);
    }
}
