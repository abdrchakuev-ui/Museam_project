package kz.enu.museum.service;

import kz.enu.museum.model.Artist;
import kz.enu.museum.repository.ArtistRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления художниками.
 * Предоставляет методы для CRUD операций и поиска.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ArtistService {
    
    private static final Logger logger = LogManager.getLogger(ArtistService.class);
    private final ArtistRepository repository;
    
    /**
     * Конструктор сервиса.
     *
     * @param repository репозиторий для работы с художниками
     */
    public ArtistService(ArtistRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Добавляет нового художника.
     *
     * @param artist художник для добавления
     * @return добавленный художник с ID
     */
    public Artist addArtist(Artist artist) {
        if (artist == null) {
            throw new IllegalArgumentException("Художник не может быть null");
        }
        
        if (artist.getFullName() == null || artist.getFullName().isBlank()) {
            throw new IllegalArgumentException("Имя художника обязательно");
        }
        
        Artist saved = repository.save(artist);
        logger.info("Художник добавлен: " + artist.getFullName());
        return saved;
    }
    
    /**
     * Обновляет художника.
     *
     * @param artist обновлённый художник
     * @return обновлённый художник
     */
    public Artist updateArtist(Artist artist) {
        if (artist == null || artist.getId() == null) {
            throw new IllegalArgumentException("Художник и его ID обязательны");
        }
        
        Artist updated = repository.save(artist);
        logger.info("Художник обновлён: " + artist.getFullName());
        return updated;
    }
    
    /**
     * Удаляет художника по ID.
     *
     * @param artistId ID художника
     */
    public void deleteArtist(Long artistId) {
        if (repository.deleteById(artistId)) {
            logger.info("Художник удалён (ID: " + artistId + ")");
        }
    }
    
    /**
     * Получает художника по ID.
     *
     * @param artistId ID художника
     * @return Optional с художником
     */
    public Optional<Artist> getArtist(Long artistId) {
        return repository.findById(artistId);
    }
    
    /**
     * Получает всех художников.
     *
     * @return список всех художников
     */
    public List<Artist> getAllArtists() {
        return repository.findAll();
    }
    
    /**
     * Поиск художников по имени.
     *
     * @param name имя художника (частичное совпадение)
     * @return список найденных художников
     */
    public List<Artist> searchByName(String name) {
        logger.debug("Поиск художников по имени: " + name);
        return repository.findByName(name);
    }
    
    /**
     * Поиск художников по стране.
     *
     * @param country страна
     * @return список художников из этой страны
     */
    public List<Artist> findByCountry(String country) {
        logger.debug("Поиск художников по стране: " + country);
        return repository.findByCountry(country);
    }
    
    /**
     * Поиск художников, активных в определённый период.
     *
     * @param startYear начальный год
     * @param endYear конечный год
     * @return список художников периода
     */
    public List<Artist> findByPeriod(int startYear, int endYear) {
        logger.debug("Поиск художников периода: " + startYear + "-" + endYear);
        return repository.findByPeriod(startYear, endYear);
    }
    
    /**
     * Получает общее количество художников.
     *
     * @return количество художников
     */
    public long getTotalCount() {
        return repository.count();
    }
}
