package kz.enu.museum.model;

import java.math.BigDecimal;

/**
 * Класс для представления выставки/экспоната.
 * Наследует от MuseumItem и добавляет специфичные для экспонатов атрибуты.
 * Демонстрирует принцип наследования.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Exhibit extends MuseumItem {
    
    private Artist author;
    private Category category;
    private Location location;
    private BigDecimal estimatedValue;
    private String imagePath;
    
    /**
     * Конструктор по умолчанию.
     */
    public Exhibit() {
        super();
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param name название экспоната
     * @param author автор/художник
     * @param category категория
     */
    public Exhibit(String name, Artist author, Category category) {
        super();
        this.setName(name);
        this.author = author;
        this.category = category;
    }
    
    // Геттеры и сеттеры
    
    public Artist getAuthor() {
        return author;
    }
    
    public void setAuthor(Artist author) {
        this.author = author;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        this.category = category;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }
    
    public void setEstimatedValue(BigDecimal estimatedValue) {
        if (estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Оценочная стоимость не может быть отрицательной");
        }
        this.estimatedValue = estimatedValue;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    /**
     * Получает информацию об экспонате для отображения.
     *
     * @return отформатированная строка с информацией
     */
    @Override
    public String getDisplayInfo() {
        return String.format("Экспонат: %s, Категория: %s, Автор: %s",
            this.getName(),
            category != null ? category.getName() : "N/A",
            author != null ? author.getFullName() : "Неизвестен"
        );
    }
    
    @Override
    public String toString() {
        return "Exhibit{" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", author=" + author +
                ", category=" + category +
                ", status=" + this.getStatus() +
                '}';
    }
}
