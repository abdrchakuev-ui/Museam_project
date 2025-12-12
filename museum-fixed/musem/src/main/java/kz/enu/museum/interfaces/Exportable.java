package kz.enu.museum.interfaces;

import java.util.Map;

/**
 * Интерфейс для объектов, поддерживающих экспорт в различные форматы.
 * Предоставляет методы для сериализации в JSON, CSV и Map.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public interface Exportable {
    
    /**
     * Экспортирует объект в JSON строку.
     *
     * @return JSON представление объекта
     */
    String toJson();
    
    /**
     * Экспортирует объект в CSV строку.
     *
     * @return CSV представление объекта
     */
    String toCsv();
    
    /**
     * Конвертирует объект в Map для дальнейшей обработки.
     *
     * @return Map с данными объекта
     */
    Map<String, Object> toMap();
}
