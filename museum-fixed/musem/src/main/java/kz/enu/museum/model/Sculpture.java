package kz.enu.museum.model;

/**
 * Класс для представления скульптуры.
 * Наследует от Exhibit и добавляет специфичные для скульптур атрибуты.
 * Демонстрирует многоуровневое наследование и полиморфизм.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Sculpture extends Exhibit {
    
    private String material; // мрамор, бронза, дерево, гранит и т.д.
    private double weight; // вес в килограммах
    private double height; // высота в сантиметрах
    
    /**
     * Конструктор по умолчанию.
     */
    public Sculpture() {
        super();
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param name название скульптуры
     * @param author автор
     * @param category категория
     * @param material материал изготовления
     * @param weight вес в кг
     * @param height высота в см
     */
    public Sculpture(String name, Artist author, Category category,
                     String material, double weight, double height) {
        super(name, author, category);
        this.material = material;
        this.weight = weight;
        this.height = height;
    }
    
    // Геттеры и сеттеры
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        if (material == null || material.isBlank()) {
            throw new IllegalArgumentException("Материал не может быть пустым");
        }
        this.material = material;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Вес должен быть положительным числом");
        }
        this.weight = weight;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Высота должна быть положительным числом");
        }
        this.height = height;
    }
    
    /**
     * Переопределённый метод для получения информации скульптуры.
     * Демонстрирует полиморфизм.
     *
     * @return отформатированная строка с информацией о скульптуре
     */
    @Override
    public String getDisplayInfo() {
        return String.format("Скульптура: %s, Материал: %s, Высота: %.1f см, Вес: %.1f кг",
            this.getName(),
            material,
            height,
            weight
        );
    }
    
    @Override
    public String toString() {
        return "Sculpture{" +
                "name='" + this.getName() + '\'' +
                ", material='" + material + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                '}';
    }
}
