package kz.enu.museum.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.Exhibit;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.model.enums.ExhibitStatus;

/**
 * Сервис для поиска и фильтрации экспонатов.
 * Предоставляет комплексные методы для поиска с использованием интерфейса Searchable.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class SearchService {
    
    private static final Logger logger = LogManager.getLogger(SearchService.class);
    private final List<MuseumItem> exhibits;
    
    /**
     * Конструктор сервиса.
     *
     * @param exhibits список экспонатов для поиска
     */
    public SearchService(List<MuseumItem> exhibits) {
        this.exhibits = exhibits != null ? exhibits : new ArrayList<>();
    }
    
    /**
     * Простой поиск по всем полям.
     *
     * @param query поисковый запрос
     * @return список найденных экспонатов
     */
    public List<MuseumItem> search(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(exhibits);
        }
        
        logger.debug("Поиск: " + query);
        return exhibits.stream()
                .filter(item -> item.matchesSearch(query))
                .collect(Collectors.toList());
    }
    
    /**
     * Комбинированный поиск с фильтрами.
     *
     * @param query поисковый запрос
     * @param category категория
     * @param author автор
     * @param status статус
     * @return список найденных экспонатов
     */
    public List<MuseumItem> advancedSearch(String query, Category category, Artist author, ExhibitStatus status) {
        List<MuseumItem> result = new ArrayList<>(exhibits);
        
        // Фильтр по поисковому запросу
        if (query != null && !query.isBlank()) {
            result = result.stream()
                    .filter(item -> item.matchesSearch(query))
                    .collect(Collectors.toList());
        }
        
        // Фильтр по категории
        if (category != null) {
            result = result.stream()
                    .filter(item -> item instanceof Exhibit)
                    .map(item -> (Exhibit) item)
                    .filter(exhibit -> exhibit.getCategory() != null && 
                            exhibit.getCategory().getId().equals(category.getId()))
                    .collect(Collectors.toList());
        }
        
        // Фильтр по автору
        if (author != null) {
            result = result.stream()
                    .filter(item -> item instanceof Exhibit)
                    .map(item -> (Exhibit) item)
                    .filter(exhibit -> exhibit.getAuthor() != null && 
                            exhibit.getAuthor().getId().equals(author.getId()))
                    .collect(Collectors.toList());
        }
        
        // Фильтр по статусу
        if (status != null) {
            result = result.stream()
                    .filter(item -> item.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        logger.debug("Расширенный поиск: найдено " + result.size() + " результатов");
        return result;
    }
    
    /**
     * Поиск по году создания.
     *
     * @param startYear начальный год
     * @param endYear конечный год
     * @return список экспонатов в диапазоне лет
     */
    public List<MuseumItem> findByCreationPeriod(int startYear, int endYear) {
        return exhibits.stream()
                .filter(item -> item.getCreationDate() != null)
                .filter(item -> {
                    int year = item.getCreationDate().getYear();
                    return year >= startYear && year <= endYear;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск по году поступления.
     *
     * @param startYear начальный год
     * @param endYear конечный год
     * @return список экспонатов в диапазоне лет
     */
    public List<MuseumItem> findByAcquisitionPeriod(int startYear, int endYear) {
        return exhibits.stream()
                .filter(item -> item.getAcquisitionDate() != null)
                .filter(item -> {
                    int year = item.getAcquisitionDate().getYear();
                    return year >= startYear && year <= endYear;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск по коду инвентарного номера.
     *
     * @param code начало инвентарного номера
     * @return список найденных экспонатов
     */
    public List<MuseumItem> findByInventoryNumberStart(String code) {
        if (code == null || code.isBlank()) {
            return new ArrayList<>();
        }
        
        return exhibits.stream()
                .filter(item -> item.getInventoryNumber().startsWith(code))
                .collect(Collectors.toList());
    }
    
    /**
     * Обновляет список экспонатов для поиска.
     *
     * @param newExhibits новый список экспонатов
     */
    public void updateExhibits(List<MuseumItem> newExhibits) {
        exhibits.clear();
        if (newExhibits != null) {
            exhibits.addAll(newExhibits);
        }
    }
}
