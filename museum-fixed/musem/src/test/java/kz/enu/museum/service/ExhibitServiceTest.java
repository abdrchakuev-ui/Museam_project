package kz.enu.museum.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.enu.museum.exception.DuplicateInventoryNumberException;
import kz.enu.museum.exception.ExhibitNotFoundException;
import kz.enu.museum.exception.InvalidDataException;
import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.Exhibit;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.model.Painting;
import kz.enu.museum.repository.ExhibitRepository;

/**
 * Тесты для сервиса управления экспонатами.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
@DisplayName("Тесты ExhibitService")
class ExhibitServiceTest {
    
    private ExhibitService exhibitService;
    private ExhibitRepository exhibitRepository;
    private Category testCategory;
    private Artist testArtist;
    
    @BeforeEach
    void setUp() {
        exhibitRepository = new ExhibitRepository();
        exhibitService = new ExhibitService(exhibitRepository);
        
        // Создание тестовых данных
        testCategory = new Category("Живопись", "ПЛ");
        testCategory.setId(1L);
        
        testArtist = new Artist("Винсент ван Гог", 1853, "Нидерланды");
        testArtist.setId(1L);
    }
    
    @Test
    @DisplayName("Добавление экспоната - успешно")
    void testAddExhibit_Success() {
        // Arrange
        Painting painting = new Painting("Звёздная ночь", testArtist, testCategory, "масло", 74, 92);
        
        // Act
        MuseumItem result = exhibitService.addExhibit(painting);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Звёздная ночь", result.getName());
        assertNotNull(result.getInventoryNumber());
        assertTrue(result.getInventoryNumber().contains("ПЛ"));
    }
    
    @Test
    @DisplayName("Добавление экспоната с пустым названием - ошибка")
    void testAddExhibit_EmptyName_ThrowsException() {
        // Act & Assert - ошибка выбрасывается в конструкторе, а не в сервисе
        assertThrows(IllegalArgumentException.class, () -> 
            new Painting("", testArtist, testCategory, "масло", 74, 92)
        );
    }
    
    @Test
    @DisplayName("Добавление экспоната без категории - ошибка")
    void testAddExhibit_NoCategory_ThrowsException() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, null, "масло", 74, 92);
        
        // Act & Assert
        assertThrows(InvalidDataException.class, () -> exhibitService.addExhibit(painting));
    }
    
    @Test
    @DisplayName("Дублирование инвентарного номера - ошибка")
    void testAddExhibit_DuplicateInventoryNumber_ThrowsException() {
        // Arrange
        Painting painting1 = new Painting("Картина 1", testArtist, testCategory, "масло", 74, 92);
        painting1.setInventoryNumber("МУЗ-ПЛ-2025-001");
        
        Painting painting2 = new Painting("Картина 2", testArtist, testCategory, "масло", 80, 100);
        painting2.setInventoryNumber("МУЗ-ПЛ-2025-001");
        
        // Act & Assert
        exhibitService.addExhibit(painting1);
        assertThrows(DuplicateInventoryNumberException.class, () -> exhibitService.addExhibit(painting2));
    }
    
    @Test
    @DisplayName("Получение экспоната - успешно")
    void testGetExhibit_Success() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        MuseumItem added = exhibitService.addExhibit(painting);
        
        // Act
        MuseumItem retrieved = exhibitService.getExhibit(added.getId());
        
        // Assert
        assertNotNull(retrieved);
        assertEquals(added.getId(), retrieved.getId());
        assertEquals("Картина", retrieved.getName());
    }
    
    @Test
    @DisplayName("Получение несуществующего экспоната - ошибка")
    void testGetExhibit_NotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ExhibitNotFoundException.class, () -> exhibitService.getExhibit(999L));
    }
    
    @Test
    @DisplayName("Удаление экспоната - успешно")
    void testDeleteExhibit_Success() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        MuseumItem added = exhibitService.addExhibit(painting);
        
        // Act
        exhibitService.deleteExhibit(added.getId());
        
        // Assert
        assertThrows(ExhibitNotFoundException.class, () -> exhibitService.getExhibit(added.getId()));
    }
    
    @Test
    @DisplayName("Удаление несуществующего экспоната - ошибка")
    void testDeleteExhibit_NotFound_ThrowsException() {
        // Act & Assert
        assertThrows(ExhibitNotFoundException.class, () -> exhibitService.deleteExhibit(999L));
    }
    
    @Test
    @DisplayName("Поиск по названию - найден")
    void testSearchByName_Found() {
        // Arrange
        Painting painting = new Painting("Звёздная ночь", testArtist, testCategory, "масло", 74, 92);
        exhibitService.addExhibit(painting);
        
        // Act
        List<MuseumItem> results = exhibitService.searchByName("Звёздная");
        
        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(item -> item.getName().contains("Звёздная")));
    }
    
    @Test
    @DisplayName("Поиск по названию - не найден")
    void testSearchByName_NotFound() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        exhibitService.addExhibit(painting);
        
        // Act
        List<MuseumItem> results = exhibitService.searchByName("Несуществующая");
        
        // Assert
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("Получение всех экспонатов")
    void testGetAllExhibits() {
        // Arrange
        Painting painting1 = new Painting("Картина 1", testArtist, testCategory, "масло", 74, 92);
        Painting painting2 = new Painting("Картина 2", testArtist, testCategory, "акварель", 80, 100);
        
        exhibitService.addExhibit(painting1);
        exhibitService.addExhibit(painting2);
        
        // Act
        List<MuseumItem> allExhibits = exhibitService.getAllExhibits();
        
        // Assert
        assertEquals(2, allExhibits.size());
    }
    
    @Test
    @DisplayName("Обновление экспоната - успешно")
    void testUpdateExhibit_Success() {
        // Arrange
        Painting painting = new Painting("Оригинальное название", testArtist, testCategory, "масло", 74, 92);
        MuseumItem added = exhibitService.addExhibit(painting);
        
        // Act
        added.setName("Обновлённое название");
        MuseumItem updated = exhibitService.updateExhibit(added);
        
        // Assert
        assertEquals("Обновлённое название", updated.getName());
    }
    
    @Test
    @DisplayName("Фильтр по категории")
    void testFilterByCategory() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        exhibitService.addExhibit(painting);
        
        // Act
        List<MuseumItem> results = exhibitService.filterByCategory(testCategory);
        
        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(item -> item instanceof Exhibit && 
                   ((Exhibit)item).getCategory().getId().equals(testCategory.getId())));
    }
    
    @Test
    @DisplayName("Подсчёт экспонатов")
    void testGetTotalCount() {
        // Arrange
        Painting painting1 = new Painting("Картина 1", testArtist, testCategory, "масло", 74, 92);
        Painting painting2 = new Painting("Картина 2", testArtist, testCategory, "масло", 80, 100);
        
        exhibitService.addExhibit(painting1);
        exhibitService.addExhibit(painting2);
        
        // Act
        long count = exhibitService.getTotalCount();
        
        // Assert
        assertEquals(2, count);
    }
}
