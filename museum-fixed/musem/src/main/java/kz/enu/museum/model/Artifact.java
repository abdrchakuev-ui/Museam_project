package kz.enu.museum.model;

/**
 * Класс для представления артефакта.
 * Наследует от Exhibit и добавляет специфичные для артефактов атрибуты.
 * Демонстрирует многоуровневое наследование и полиморфизм.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Artifact extends Exhibit {
    
    private String origin; // происхождение (древний Египет, древний Рим и т.д.)
    private String period; // период/эпоха
    private String material; // материал артефакта
    private double age; // возраст в годах (примерно)
    
    /**
     * Конструктор по умолчанию.
     */
    public Artifact() {
        super();
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param name название артефакта
     * @param author автор/неизвестен
     * @param category категория
     * @param origin происхождение
     * @param period период
     * @param material материал
     */
    public Artifact(String name, Artist author, Category category,
                    String origin, String period, String material) {
        super(name, author, category);
        this.origin = origin;
        this.period = period;
        this.material = material;
    }
    
    // Геттеры и сеттеры
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("Происхождение не может быть пустым");
        }
        this.origin = origin;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("Период не может быть пустым");
        }
        this.period = period;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        if (material == null || material.isBlank()) {
            throw new IllegalArgumentException("Материал не может быть пустым");
        }
        this.material = material;
    }
    
    public double getAge() {
        return age;
    }
    
    public void setAge(double age) {
        if (age < 0) {
            throw new IllegalArgumentException("Возраст не может быть отрицательным");
        }
        this.age = age;
    }
    
    /**
     * Переопределённый метод для получения информации артефакта.
     * Демонстрирует полиморфизм.
     *
     * @return отформатированная строка с информацией об артефакте
     */
    @Override
    public String getDisplayInfo() {
        return String.format("Артефакт: %s, Происхождение: %s, Период: %s, Материал: %s, Возраст: ~%.0f лет",
            this.getName(),
            origin,
            period,
            material,
            age
        );
    }
    
    @Override
    public String toString() {
        return "Artifact{" +
                "name='" + this.getName() + '\'' +
                ", origin='" + origin + '\'' +
                ", period='" + period + '\'' +
                ", material='" + material + '\'' +
                ", age=" + age +
                '}';
    }
}
