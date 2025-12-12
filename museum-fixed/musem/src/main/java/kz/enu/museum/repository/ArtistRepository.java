package kz.enu.museum.repository;

import kz.enu.museum.model.Artist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Репозиторий для работы с художниками.
 * Реализует операции по хранению и поиску художников.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ArtistRepository implements Repository<Artist> {
    
    private static final Logger logger = LogManager.getLogger(ArtistRepository.class);
    private final Map<Long, Artist> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Artist save(Artist entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Художник не может быть null");
        }
        
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
            logger.info("Добавлен новый художник: " + entity.getFullName());
        } else {
            logger.info("Обновлён художник: " + entity.getFullName());
        }
        
        storage.put(entity.getId(), entity);
        return entity;
    }
    
    @Override
    public Optional<Artist> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public List<Artist> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (storage.containsKey(id)) {
            Artist removed = storage.remove(id);
            logger.info("Удалён художник: " + removed.getFullName());
            return true;
        }
        logger.warn("Попытка удаления несуществующего художника (ID: " + id + ")");
        return false;
    }
    
    @Override
    public long count() {
        return storage.size();
    }
    
    @Override
    public void deleteAll() {
        logger.info("Удалены все художники (" + storage.size() + " шт)");
        storage.clear();
    }
    
    /**
     * Поиск художников по имени.
     *
     * @param name имя (частичное совпадение)
     * @return список найденных художников
     */
    public List<Artist> findByName(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }
        
        String lowerName = name.toLowerCase();
        return storage.values().stream()
                .filter(artist -> artist.getFullName().toLowerCase().contains(lowerName))
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск художников по стране.
     *
     * @param country страна
     * @return список художников из этой страны
     */
    public List<Artist> findByCountry(String country) {
        if (country == null || country.isBlank()) {
            return new ArrayList<>();
        }
        
        return storage.values().stream()
                .filter(artist -> country.equals(artist.getCountry()))
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск художников, работавших в определённый период.
     *
     * @param startYear начальный год
     * @param endYear конечный год
     * @return список художников, активных в этот период
     */
    public List<Artist> findByPeriod(int startYear, int endYear) {
        return storage.values().stream()
                .filter(artist -> {
                    int birthYear = artist.getBirthYear();
                    Integer deathYear = artist.getDeathYear();
                    
                    // Художник активен если: рожден до конца периода и (не умер или умер после начала)
                    return birthYear <= endYear && (deathYear == null || deathYear >= startYear);
                })
                .collect(Collectors.toList());
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
