package kz.enu.museum.interfaces;

import java.util.List;

/**
 * Интерфейс для объектов, поддерживающих поиск.
 * Предоставляет методы для проверки соответствия поисковому запросу.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public interface Searchable {
    
    /**
     * Проверяет, соответствует ли объект поисковому запросу.
     *
     * @param query поисковый запрос (регистронезависимый)
     * @return true, если объект соответствует запросу, false иначе
     */
    boolean matchesSearch(String query);
    
    /**
     * Возвращает список полей, по которым выполняется поиск.
     *
     * @return список названий полей для поиска
     */
    List<String> getSearchableFields();
}
