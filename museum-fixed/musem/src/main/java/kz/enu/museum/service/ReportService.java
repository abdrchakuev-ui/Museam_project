package kz.enu.museum.service;

import kz.enu.museum.model.*;
import kz.enu.museum.model.enums.ExhibitStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для генерации отчётов.
 * Предоставляет методы для создания различных статистических отчётов.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ReportService {
    
    private static final Logger logger = LogManager.getLogger(ReportService.class);
    private final List<MuseumItem> exhibits;
    private final CategoryService categoryService;
    private final ArtistService artistService;
    
    /**
     * Конструктор сервиса.
     *
     * @param exhibits список экспонатов
     * @param categoryService сервис категорий
     * @param artistService сервис художников
     */
    public ReportService(List<MuseumItem> exhibits, CategoryService categoryService, ArtistService artistService) {
        this.exhibits = exhibits != null ? exhibits : new ArrayList<>();
        this.categoryService = categoryService;
        this.artistService = artistService;
    }
    
    /**
     * Генерирует общую статистику.
     *
     * @return Map со статистикой
     */
    public Map<String, Object> generateGeneralStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("Всего экспонатов", exhibits.size());
        stats.put("Категорий", categoryService.getAllCategories().size());
        stats.put("Художников", artistService.getAllArtists().size());
        
        // Статистика по статусам
        Map<ExhibitStatus, Long> statusStats = exhibits.stream()
                .collect(Collectors.groupingBy(MuseumItem::getStatus, Collectors.counting()));
        
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (ExhibitStatus status : ExhibitStatus.values()) {
            statusMap.put(status.getDisplayName(), statusStats.getOrDefault(status, 0L));
        }
        stats.put("Распределение по статусам", statusMap);
        
        // Типы экспонатов
        Map<String, Long> typeStats = exhibits.stream()
                .collect(Collectors.groupingBy(item -> item.getClass().getSimpleName(), Collectors.counting()));
        stats.put("Распределение по типам", typeStats);
        
        logger.info("Сгенерирована общая статистика");
        return stats;
    }
    
    /**
     * Получает статистику по категориям.
     *
     * @return Map со статистикой по категориям
     */
    public Map<String, Long> getCategoryStatistics() {
        Map<String, Long> stats = new LinkedHashMap<>();
        
        exhibits.stream()
                .filter(item -> item instanceof Exhibit)
                .map(item -> (Exhibit) item)
                .filter(exhibit -> exhibit.getCategory() != null)
                .collect(Collectors.groupingBy(
                        exhibit -> exhibit.getCategory().getName(),
                        Collectors.counting()
                ))
                .forEach(stats::put);
        
        logger.info("Сгенерирована статистика по категориям");
        return stats;
    }
    
    /**
     * Получает экспонаты по определённому статусу.
     *
     * @param status статус
     * @return список экспонатов с этим статусом
     */
    public List<MuseumItem> getExhibitsByStatus(ExhibitStatus status) {
        return exhibits.stream()
                .filter(item -> item.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Получает экспонаты конкретного художника.
     *
     * @param artist художник
     * @return список экспонатов этого художника
     */
    public List<MuseumItem> getExhibitsByArtist(Artist artist) {
        return exhibits.stream()
                .filter(item -> item instanceof Exhibit)
                .map(item -> (Exhibit) item)
                .filter(exhibit -> exhibit.getAuthor() != null && 
                        exhibit.getAuthor().getId().equals(artist.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Получает экспонаты в конкретном зале.
     *
     * @param location местоположение (зал)
     * @return список экспонатов в этом зале
     */
    public List<MuseumItem> getExhibitsByLocation(Location location) {
        return exhibits.stream()
                .filter(item -> item instanceof Exhibit)
                .map(item -> (Exhibit) item)
                .filter(exhibit -> exhibit.getLocation() != null && 
                        exhibit.getLocation().getId().equals(location.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Экспортирует отчёт в CSV формат.
     *
     * @param items список экспонатов
     * @return CSV строка
     */
    public String exportToCSV(List<MuseumItem> items) {
        StringBuilder csv = new StringBuilder();
        
        // Заголовок
        csv.append("ID,Название,Тип,Статус,Инвентарный номер\n");
        
        // Данные
        for (MuseumItem item : items) {
            csv.append(item.getId()).append(",")
                    .append(escapeCSV(item.getName())).append(",")
                    .append(item.getClass().getSimpleName()).append(",")
                    .append(item.getStatus().getDisplayName()).append(",")
                    .append(item.getInventoryNumber()).append("\n");
        }
        
        logger.info("Экспортировано " + items.size() + " экспонатов в CSV");
        return csv.toString();
    }
    
    /**
     * Экранирует специальные символы для CSV.
     *
     * @param value значение
     * @return экранированное значение
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Получает количество экспонатов по статусам.
     *
     * @return Map с количеством по статусам
     */
    public Map<String, Integer> getStatusSummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        
        for (ExhibitStatus status : ExhibitStatus.values()) {
            long count = exhibits.stream()
                    .filter(item -> item.getStatus() == status)
                    .count();
            summary.put(status.getDisplayName(), (int) count);
        }
        
        return summary;
    }
}
