package kz.enu.museum.repository;

import java.util.List;
import java.util.Optional;

/**
 * Обобщённый интерфейс репозитория для работы с сущностями.
 * Определяет базовые CRUD операции.
 *
 * @param <T> тип сущности
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public interface Repository<T> {
    
    /**
     * Сохраняет сущность (добавляет или обновляет).
     *
     * @param entity сущность для сохранения
     * @return сохранённая сущность с ID
     */
    T save(T entity);
    
    /**
     * Находит сущность по ID.
     *
     * @param id идентификатор
     * @return Optional с сущностью
     */
    Optional<T> findById(Long id);
    
    /**
     * Возвращает все сущности.
     *
     * @return список всех сущностей
     */
    List<T> findAll();
    
    /**
     * Удаляет сущность по ID.
     *
     * @param id идентификатор сущности
     * @return true если сущность была удалена
     */
    boolean deleteById(Long id);
    
    /**
     * Возвращает количество сущностей.
     *
     * @return количество записей
     */
    long count();
    
    /**
     * Удаляет все сущности.
     */
    void deleteAll();
}
