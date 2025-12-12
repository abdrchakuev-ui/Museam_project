package kz.enu.museum.model;

/**
 * Класс для представления географического местоположения экспоната в музее.
 * Описывает зал, номер витрины и вместимость.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class Location {
    private Long id;
    private String hallName;
    private String displayNumber;
    private int capacity;
    private int currentCount;
    
    /**
     * Конструктор по умолчанию.
     */
    public Location() {
    }
    
    /**
     * Конструктор с основными параметрами.
     *
     * @param hallName название зала
     * @param displayNumber номер витрины/выставочной площади
     * @param capacity максимальная вместимость
     */
    public Location(String hallName, String displayNumber, int capacity) {
        this.hallName = hallName;
        this.displayNumber = displayNumber;
        this.capacity = capacity;
        this.currentCount = 0;
    }
    
    /**
     * Полный конструктор.
     *
     * @param id уникальный идентификатор
     * @param hallName название зала
     * @param displayNumber номер витрины
     * @param capacity максимальная вместимость
     * @param currentCount текущее количество экспонатов
     */
    public Location(Long id, String hallName, String displayNumber, int capacity, int currentCount) {
        this.id = id;
        this.hallName = hallName;
        this.displayNumber = displayNumber;
        this.capacity = capacity;
        this.currentCount = currentCount;
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getHallName() {
        return hallName;
    }
    
    public void setHallName(String hallName) {
        if (hallName == null || hallName.isBlank()) {
            throw new IllegalArgumentException("Название зала не может быть пустым");
        }
        this.hallName = hallName;
    }
    
    public String getDisplayNumber() {
        return displayNumber;
    }
    
    public void setDisplayNumber(String displayNumber) {
        if (displayNumber == null || displayNumber.isBlank()) {
            throw new IllegalArgumentException("Номер витрины не может быть пустым");
        }
        this.displayNumber = displayNumber;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Вместимость должна быть больше нуля");
        }
        this.capacity = capacity;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public void setCurrentCount(int currentCount) {
        if (currentCount < 0 || currentCount > capacity) {
            throw new IllegalArgumentException("Текущее количество должно быть от 0 до вместимости");
        }
        this.currentCount = currentCount;
    }
    
    /**
     * Проверяет, есть ли место в этом местоположении.
     *
     * @return true если есть свободное место
     */
    public boolean hasSpace() {
        return currentCount < capacity;
    }
    
    @Override
    public String toString() {
        return hallName + " (" + displayNumber + ", " + currentCount + "/" + capacity + ")";
    }
}
