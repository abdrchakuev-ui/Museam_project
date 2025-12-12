package kz.enu.museum.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kz.enu.museum.exception.DuplicateInventoryNumberException;
import kz.enu.museum.exception.ExhibitNotFoundException;
import kz.enu.museum.exception.InvalidDataException;
import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.Exhibit;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.repository.ExhibitRepository;
import kz.enu.museum.util.InventoryNumberGenerator;

/**
 * Сервис для управления экспонатами.
 * Предоставляет методы для CRUD операций, поиска и фильтрации.
 * Реализует бизнес-логику приложения.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ExhibitService {
    
    private static final Logger logger = LogManager.getLogger(ExhibitService.class);
    private final ExhibitRepository repository;
    
    /**
     * Конструктор сервиса.
     *
     * @param repository репозиторий для работы с экспонатами
     */
    public ExhibitService(ExhibitRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Добавляет новый экспонат в коллекцию.
     * Автоматически генерирует инвентарный номер.
     *
     * @param exhibit экспонат для добавления (не null)
     * @return добавленный экспонат с присвоенным ID и инвентарным номером
     * @throws InvalidDataException если данные экспоната некорректны
     * @throws DuplicateInventoryNumberException если инвентарный номер уже существует
     */
    public MuseumItem addExhibit(Exhibit exhibit) throws InvalidDataException, DuplicateInventoryNumberException {
        if (exhibit == null) {
            throw new InvalidDataException("Экспонат не может быть null");
        }
        
        // Валидация
        if (exhibit.getName() == null || exhibit.getName().isBlank()) {
            throw new InvalidDataException("Название экспоната обязательно");
        }
        
        if (exhibit.getCategory() == null) {
            throw new InvalidDataException("Категория экспоната обязательна");
        }
        
        // Генерируем инвентарный номер если его ещё нет
        if (exhibit.getInventoryNumber() == null || exhibit.getInventoryNumber().isBlank()) {
            String categoryCode = exhibit.getCategory().getCategoryCode();
            String inventoryNumber = InventoryNumberGenerator.generateInventoryNumber(categoryCode);
            exhibit.setInventoryNumber(inventoryNumber);
        } else {
            // Проверяем на дубликат
            if (repository.existsByInventoryNumber(exhibit.getInventoryNumber())) {
                throw new DuplicateInventoryNumberException(
                    "Инвентарный номер '" + exhibit.getInventoryNumber() + "' уже используется"
                );
            }
        }
        
        MuseumItem saved = repository.save(exhibit);
        logger.info("Экспонат добавлен: " + exhibit.getName() + " (" + exhibit.getInventoryNumber() + ")");
        
        return saved;
    }
    
    /**
     * Обновляет существующий экспонат.
     *
     * @param exhibit экспонат с обновлёнными данными
     * @return обновлённый экспонат
     * @throws ExhibitNotFoundException если экспонат не найден
     * @throws InvalidDataException если данные некорректны
     */
    public MuseumItem updateExhibit(MuseumItem exhibit) throws ExhibitNotFoundException, InvalidDataException {
        if (exhibit == null || exhibit.getId() == null) {
            throw new InvalidDataException("Экспонат и его ID обязательны");
        }
        
        if (!repository.findById(exhibit.getId()).isPresent()) {
            throw new ExhibitNotFoundException("Экспонат с ID " + exhibit.getId() + " не найден");
        }
        
        if (exhibit.getName() == null || exhibit.getName().isBlank()) {
            throw new InvalidDataException("Название экспоната обязательно");
        }
        
        MuseumItem updated = repository.save(exhibit);
        logger.info("Экспонат обновлён: " + exhibit.getName());
        
        return updated;
    }
    
    /**
     * Удаляет экспонат по ID.
     *
     * @param exhibitId ID экспоната
     * @throws ExhibitNotFoundException если экспонат не найден
     */
    public void deleteExhibit(Long exhibitId) throws ExhibitNotFoundException {
        if (!repository.deleteById(exhibitId)) {
            throw new ExhibitNotFoundException("Экспонат с ID " + exhibitId + " не найден");
        }
        logger.info("Экспонат удалён (ID: " + exhibitId + ")");
    }
    
    /**
     * Получает экспонат по ID.
     *
     * @param exhibitId ID экспоната
     * @return экспонат
     * @throws ExhibitNotFoundException если экспонат не найден
     */
    public MuseumItem getExhibit(Long exhibitId) throws ExhibitNotFoundException {
        return repository.findById(exhibitId)
                .orElseThrow(() -> new ExhibitNotFoundException("Экспонат с ID " + exhibitId + " не найден"));
    }
    
    /**
     * Получает все экспонаты.
     *
     * @return список всех экспонатов
     */
    public List<MuseumItem> getAllExhibits() {
        return repository.findAll();
    }
    
    /**
     * Поиск экспонатов по названию.
     *
     * @param name название (частичное совпадение, регистронезависимый)
     * @return список найденных экспонатов
     */
    public List<MuseumItem> searchByName(String name) {
        logger.debug("Поиск экспонатов по названию: " + name);
        return repository.findByName(name);
    }
    
    /**
     * Поиск по категории.
     *
     * @param category категория
     * @return список экспонатов в данной категории
     */
    public List<MuseumItem> filterByCategory(Category category) {
        if (category == null) {
            return repository.findAll();
        }
        
        logger.debug("Фильтр по категории: " + category.getName());
        return repository.findAll().stream()
                .filter(item -> item instanceof Exhibit)
                .map(item -> (Exhibit) item)
                .filter(exhibit -> exhibit.getCategory() != null && 
                        exhibit.getCategory().getId().equals(category.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск по автору.
     *
     * @param author автор
     * @return список экспонатов этого автора
     */
    public List<MuseumItem> filterByAuthor(Artist author) {
        if (author == null) {
            return repository.findAll();
        }
        
        logger.debug("Фильтр по автору: " + author.getFullName());
        return repository.findAll().stream()
                .filter(item -> item instanceof Exhibit)
                .map(item -> (Exhibit) item)
                .filter(exhibit -> exhibit.getAuthor() != null && 
                        exhibit.getAuthor().getId().equals(author.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Фильтр по статусу.
     *
     * @param status статус
     * @return список экспонатов с данным статусом
     */
    public List<MuseumItem> filterByStatus(kz.enu.museum.model.enums.ExhibitStatus status) {
        logger.debug("Фильтр по статусу: " + status);
        return repository.findAll().stream()
                .filter(item -> item.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Получает общее количество экспонатов.
     *
     * @return количество экспонатов
     */
    public long getTotalCount() {
        return repository.count();
    }
}
