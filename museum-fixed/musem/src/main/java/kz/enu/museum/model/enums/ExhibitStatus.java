package kz.enu.museum.model.enums;

/**
 * Перечисление статусов экспоната в музее.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public enum ExhibitStatus {
    ON_DISPLAY("На выставке"),
    IN_STORAGE("В хранилище"),
    ON_RESTORATION("На реставрации"),
    ON_LOAN("В аренде");
    
    private final String displayName;
    
    /**
     * Конструктор enum с отображаемым названием.
     *
     * @param displayName название для отображения в UI
     */
    ExhibitStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Возвращает отображаемое название статуса.
     *
     * @return название статуса
     */
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
