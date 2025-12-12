package kz.enu.museum.model;

/**
 * Класс для представления картины.
 * Наследует от Exhibit и добавляет специфичные для живописи атрибуты.
 * Демонстрирует многоуровневое наследование и полиморфизм.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Painting extends Exhibit {
    
    private String technique; // масло, акварель, темпера, гуашь и т.д.
    private int width; // ширина в см
    private int height; // высота в см
    private boolean hasFrame; // наличие рамы
    
    /**
     * Конструктор по умолчанию.
     */
    public Painting() {
        super();
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param name название картины
     * @param author автор
     * @param category категория
     * @param technique техника исполнения
     * @param width ширина
     * @param height высота
     */
    public Painting(String name, Artist author, Category category, 
                    String technique, int width, int height) {
        super(name, author, category);
        this.technique = technique;
        this.width = width;
        this.height = height;
        this.hasFrame = false;
    }
    
    // Геттеры и сеттеры
    
    public String getTechnique() {
        return technique;
    }
    
    public void setTechnique(String technique) {
        if (technique == null || technique.isBlank()) {
            throw new IllegalArgumentException("Техника не может быть пустой");
        }
        this.technique = technique;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Ширина должна быть положительным числом");
        }
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Высота должна быть положительным числом");
        }
        this.height = height;
    }
    
    public boolean isHasFrame() {
        return hasFrame;
    }
    
    public void setHasFrame(boolean hasFrame) {
        this.hasFrame = hasFrame;
    }
    
    /**
     * Переопределённый метод для получения информации картины.
     * Демонстрирует полиморфизм.
     *
     * @return отформатированная строка с информацией о картине
     */
    @Override
    public String getDisplayInfo() {
        return String.format("Картина: %s, Техника: %s, Размер: %dx%d см%s",
            this.getName(),
            technique,
            width,
            height,
            hasFrame ? ", в раме" : ", без рамы"
        );
    }
    
    @Override
    public String toString() {
        return "Painting{" +
                "name='" + this.getName() + '\'' +
                ", technique='" + technique + '\'' +
                ", size=" + width + "x" + height +
                ", frame=" + hasFrame +
                '}';
    }
}
