package kz.enu.museum.repository;

import kz.enu.museum.model.MuseumItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Репозиторий для работы с экспонатами (Exhibit).
 * Реализует операции по хранению, поиску и удалению экспонатов.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ExhibitRepository implements Repository<MuseumItem> {
    
    private static final Logger logger = LogManager.getLogger(ExhibitRepository.class);
    private final Map<Long, MuseumItem> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public MuseumItem save(MuseumItem entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Экспонат не может быть null");
        }
        
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
            logger.info("Добавлен новый экспонат: " + entity.getName() + " (ID: " + entity.getId() + ")");
        } else {
            logger.info("Обновлён экспонат: " + entity.getName() + " (ID: " + entity.getId() + ")");
        }
        
        storage.put(entity.getId(), entity);
        return entity;
    }
    
    @Override
    public Optional<MuseumItem> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public List<MuseumItem> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (storage.containsKey(id)) {
            MuseumItem removed = storage.remove(id);
            logger.info("Удалён экспонат: " + removed.getName() + " (ID: " + id + ")");
            return true;
        }
        logger.warn("Попытка удаления несуществующего экспоната (ID: " + id + ")");
        return false;
    }
    
    @Override
    public long count() {
        return storage.size();
    }
    
    @Override
    public void deleteAll() {
        logger.info("Удалены все экспонаты (" + storage.size() + " шт)");
        storage.clear();
    }
    
    /**
     * Поиск экспонатов по названию.
     *
     * @param name название (частичное совпадение)
     * @return список найденных экспонатов
     */
    public List<MuseumItem> findByName(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }
        
        String lowerName = name.toLowerCase();
        return storage.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerName))
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск экспонатов по инвентарному номеру.
     *
     * @param inventoryNumber инвентарный номер
     * @return Optional с найденным экспонатом
     */
    public Optional<MuseumItem> findByInventoryNumber(String inventoryNumber) {
        if (inventoryNumber == null || inventoryNumber.isBlank()) {
            return Optional.empty();
        }
        
        return storage.values().stream()
                .filter(item -> inventoryNumber.equals(item.getInventoryNumber()))
                .findFirst();
    }
    
    /**
     * Проверяет существование инвентарного номера.
     *
     * @param inventoryNumber инвентарный номер
     * @return true если номер уже используется
     */
    public boolean existsByInventoryNumber(String inventoryNumber) {
        return findByInventoryNumber(inventoryNumber).isPresent();
    }
    
    /**
     * Устанавливает следующий ID для генератора.
     *
     * @param nextId следующий ID
     */
    public void setNextId(Long nextId) {
        if (nextId > idGenerator.get()) {
            idGenerator.set(nextId);
        }
    }
}
