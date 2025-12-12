package kz.enu.museum.repository;

import kz.enu.museum.model.Category;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Репозиторий для работы с категориями.
 * Реализует операции по хранению и поиску категорий.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class CategoryRepository implements Repository<Category> {
    
    private static final Logger logger = LogManager.getLogger(CategoryRepository.class);
    private final Map<Long, Category> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Category save(Category entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
            logger.info("Добавлена новая категория: " + entity.getName());
        } else {
            logger.info("Обновлена категория: " + entity.getName());
        }
        
        storage.put(entity.getId(), entity);
        return entity;
    }
    
    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public List<Category> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (storage.containsKey(id)) {
            Category removed = storage.remove(id);
            logger.info("Удалена категория: " + removed.getName());
            return true;
        }
        logger.warn("Попытка удаления несуществующей категории (ID: " + id + ")");
        return false;
    }
    
    @Override
    public long count() {
        return storage.size();
    }
    
    @Override
    public void deleteAll() {
        logger.info("Удалены все категории (" + storage.size() + " шт)");
        storage.clear();
    }
    
    /**
     * Поиск категорий по названию.
     *
     * @param name название (частичное совпадение)
     * @return список найденных категорий
     */
    public List<Category> findByName(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }
        
        String lowerName = name.toLowerCase();
        return storage.values().stream()
                .filter(cat -> cat.getName().toLowerCase().contains(lowerName))
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск категории по коду.
     *
     * @param categoryCode код категории
     * @return Optional с найденной категорией
     */
    public Optional<Category> findByCode(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return Optional.empty();
        }
        
        return storage.values().stream()
                .filter(cat -> categoryCode.equals(cat.getCategoryCode()))
                .findFirst();
    }
    
    /**
     * Возвращает корневые категории (без родителя).
     *
     * @return список корневых категорий
     */
    public List<Category> findRootCategories() {
        return storage.values().stream()
                .filter(Category::isRoot)
                .collect(Collectors.toList());
    }
    
    /**
     * Возвращает подкатегории для конкретной категории.
     *
     * @param parentId ID родительской категории
     * @return список подкатегорий
     */
    public List<Category> findSubcategories(Long parentId) {
        Optional<Category> parent = findById(parentId);
        if (parent.isEmpty()) {
            return new ArrayList<>();
        }
        
        return parent.get().getSubcategories();
    }
    
    /**
     * Устанавливает следующий ID для генератора.
     *
     * @param nextId следующий ID
     */
    public void setNextId(Long nextId) {
        if (nextId > idGenerator.get()) {
            idGenerator.set(nextId);
        }
    }
}
