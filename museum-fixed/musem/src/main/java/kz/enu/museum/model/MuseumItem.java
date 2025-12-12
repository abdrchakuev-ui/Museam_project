package kz.enu.museum.model;

import kz.enu.museum.interfaces.Exportable;
import kz.enu.museum.interfaces.Searchable;
import kz.enu.museum.model.enums.ExhibitStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Абстрактный базовый класс для всех экспонатов музея.
 * Определяет общие атрибуты и методы для всех типов экспонатов.
 * Демонстрирует принципы абстракции и полиморфизма.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public abstract class MuseumItem implements Searchable, Exportable {
    
    private Long id;
    private String name;
    private String description;
    private LocalDate creationDate;
    private LocalDate acquisitionDate;
    private String inventoryNumber; // Формат: МУЗ-{категория}-{год}-{номер}
    private ExhibitStatus status;
    
    /**
     * Конструктор по умолчанию.
     */
    public MuseumItem() {
        this.status = ExhibitStatus.IN_STORAGE;
    }
    
    /**
     * Абстрактный метод для получения информации для отображения.
     * Должен быть переопределён в подклассах.
     *
     * @return строка с информацией об экспонате
     */
    public abstract String getDisplayInfo();
    
    // Геттеры и сеттеры с валидацией (принцип инкапсуляции)
    
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
            throw new IllegalArgumentException("Название экспоната не может быть пустым");
        }
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    public LocalDate getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDate creationDate) {
        if (creationDate != null && creationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата создания не может быть в будущем");
        }
        this.creationDate = creationDate;
    }
    
    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }
    
    public void setAcquisitionDate(LocalDate acquisitionDate) {
        if (acquisitionDate != null && acquisitionDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата поступления не может быть в будущем");
        }
        this.acquisitionDate = acquisitionDate;
    }
    
    public String getInventoryNumber() {
        return inventoryNumber;
    }
    
    public void setInventoryNumber(String inventoryNumber) {
        if (inventoryNumber == null || inventoryNumber.isBlank()) {
            throw new IllegalArgumentException("Инвентарный номер не может быть пустым");
        }
        this.inventoryNumber = inventoryNumber;
    }
    
    public ExhibitStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExhibitStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус не может быть null");
        }
        this.status = status;
    }
    
    /**
     * Реализация интерфейса Searchable.
     * Проверяет соответствие объекта поисковому запросу.
     *
     * @param query поисковый запрос (регистронезависимый)
     * @return true если объект соответствует запросу
     */
    @Override
    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        return name.toLowerCase().contains(lowerQuery) ||
               description.toLowerCase().contains(lowerQuery) ||
               inventoryNumber.toLowerCase().contains(lowerQuery);
    }
    
    /**
     * Возвращает поля для поиска.
     *
     * @return список поисковых полей
     */
    @Override
    public List<String> getSearchableFields() {
        List<String> fields = new ArrayList<>();
        fields.add("name");
        fields.add("description");
        fields.add("inventoryNumber");
        return fields;
    }
    
    /**
     * Экспортирует объект в JSON (базовый формат).
     * Переопределяется в подклассах для добавления специфичных полей.
     *
     * @return JSON строка
     */
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"inventoryNumber\":\"%s\",\"status\":\"%s\"}",
            id, name, description, inventoryNumber, status
        );
    }
    
    /**
     * Экспортирует объект в CSV формат.
     *
     * @return CSV строка
     */
    @Override
    public String toCsv() {
        return String.format("%d,%s,%s,%s,%s",
            id, name, description, inventoryNumber, status
        );
    }
    
    /**
     * Конвертирует объект в Map.
     *
     * @return Map с данными
     */
    @Override
    public Map<String, Object> toMap() {
        return Map.ofEntries(
            Map.entry("id", id),
            Map.entry("name", name),
            Map.entry("description", description),
            Map.entry("creationDate", creationDate),
            Map.entry("acquisitionDate", acquisitionDate),
            Map.entry("inventoryNumber", inventoryNumber),
            Map.entry("status", status)
        );
    }
    
    @Override
    public String toString() {
        return name + " (" + inventoryNumber + ")";
    }
}
