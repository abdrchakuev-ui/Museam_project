package kz.enu.museum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления категории экспонатов.
 * Поддерживает иерархическую структуру с подкатегориями.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Category {
    private Long id;
    private String name;
    private String description;
    private String categoryCode; // например, ПЛ для Живопись, СК для Скульптура
    private Category parentCategory; // null для корневых категорий
    private List<Category> subcategories;
    
    /**
     * Конструктор по умолчанию.
     */
    public Category() {
        this.subcategories = new ArrayList<>();
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param name название категории
     * @param categoryCode код категории
     */
    public Category(String name, String categoryCode) {
        this.name = name;
        this.categoryCode = categoryCode;
        this.subcategories = new ArrayList<>();
    }
    
    /**
     * Полный конструктор.
     *
     * @param id уникальный идентификатор
     * @param name название категории
     * @param description описание
     * @param categoryCode код категории
     * @param parentCategory родительская категория
     */
    public Category(Long id, String name, String description, String categoryCode, Category parentCategory) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryCode = categoryCode;
        this.parentCategory = parentCategory;
        this.subcategories = new ArrayList<>();
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    public String getCategoryCode() {
        return categoryCode;
    }
    
    public void setCategoryCode(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new IllegalArgumentException("Код категории не может быть пустым");
        }
        this.categoryCode = categoryCode;
    }
    
    public Category getParentCategory() {
        return parentCategory;
    }
    
    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }
    
    public List<Category> getSubcategories() {
        return subcategories;
    }
    
    public void setSubcategories(List<Category> subcategories) {
        this.subcategories = subcategories != null ? subcategories : new ArrayList<>();
    }
    
    /**
     * Добавляет подкатегорию.
     *
     * @param subcategory подкатегория для добавления
     */
    public void addSubcategory(Category subcategory) {
        if (subcategory == null) {
            throw new IllegalArgumentException("Подкатегория не может быть null");
        }
        subcategory.setParentCategory(this);
        subcategories.add(subcategory);
    }
    
    /**
     * Удаляет подкатегорию.
     *
     * @param subcategory подкатегория для удаления
     * @return true если подкатегория была удалена
     */
    public boolean removeSubcategory(Category subcategory) {
        boolean removed = subcategories.remove(subcategory);
        if (removed && subcategory != null) {
            subcategory.setParentCategory(null);
        }
        return removed;
    }
    
    /**
     * Проверяет, является ли это категория корневой (не имеет родителя).
     *
     * @return true если это корневая категория
     */
    public boolean isRoot() {
        return parentCategory == null;
    }
    
    @Override
    public String toString() {
        return name + " [" + categoryCode + "]";
    }
}
