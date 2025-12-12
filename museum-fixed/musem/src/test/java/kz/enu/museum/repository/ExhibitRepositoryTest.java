package kz.enu.museum.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.enu.museum.model.Artist;
import kz.enu.museum.model.Category;
import kz.enu.museum.model.MuseumItem;
import kz.enu.museum.model.Painting;

/**
 * Тесты для репозитория экспонатов.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
@DisplayName("Тесты ExhibitRepository")
class ExhibitRepositoryTest {
    
    private ExhibitRepository repository;
    private Category testCategory;
    private Artist testArtist;
    
    @BeforeEach
    void setUp() {
        repository = new ExhibitRepository();
        
        // Создание тестовых данных
        testCategory = new Category("Живопись", "ПЛ");
        testCategory.setId(100L);  // Устанавливаем ID явно
        
        testArtist = new Artist("Винсент ван Гог", 1853, "Нидерланды");
        testArtist.setId(1L);
    }
    
    @Test
    @DisplayName("Сохранение экспоната - успешно")
    void testSave_Success() {
        // Arrange
        Painting painting = new Painting("Звёздная ночь", testArtist, testCategory, "масло", 74, 92);
        
        // Act
        MuseumItem saved = repository.save(painting);
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals("Звёздная ночь", saved.getName());
    }
    
    @Test
    @DisplayName("Сохранение null - ошибка")
    void testSave_NullExhibit_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }
    
    @Test
    @DisplayName("Поиск по ID - найден")
    void testFindById_Found() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        MuseumItem saved = repository.save(painting);
        
        // Act
        MuseumItem found = repository.findById(saved.getId()).orElse(null);
        
        // Assert
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }
    
    @Test
    @DisplayName("Поиск по ID - не найден")
    void testFindById_NotFound() {
        // Act
        MuseumItem found = repository.findById(999L).orElse(null);
        
        // Assert
        assertNull(found);
    }
    
    @Test
    @DisplayName("Получение всех экспонатов")
    void testFindAll() {
        // Arrange
        Painting painting1 = new Painting("Картина 1", testArtist, testCategory, "масло", 74, 92);
        Painting painting2 = new Painting("Картина 2", testArtist, testCategory, "акварель", 80, 100);
        
        repository.save(painting1);
        repository.save(painting2);
        
        // Act
        List<MuseumItem> all = repository.findAll();
        
        // Assert
        assertEquals(2, all.size());
    }
    
    @Test
    @DisplayName("Удаление по ID - успешно")
    void testDeleteById_Success() {
        // Arrange
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        MuseumItem saved = repository.save(painting);
        
        // Act
        repository.deleteById(saved.getId());
        MuseumItem found = repository.findById(saved.getId()).orElse(null);
        
        // Assert
        assertNull(found);
    }
    
    @Test
    @DisplayName("Удаление несуществующего ID - ошибки нет")
    void testDeleteById_NotExists() {
        // Act & Assert
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }
    
    @Test
    @DisplayName("Подсчёт экспонатов")
    void testCount() {
        // Arrange
        Painting painting1 = new Painting("Картина 1", testArtist, testCategory, "масло", 74, 92);
        Painting painting2 = new Painting("Картина 2", testArtist, testCategory, "масло", 80, 100);
        
        repository.save(painting1);
        repository.save(painting2);
        
        // Act
        long count = repository.count();
        
        // Assert
        assertEquals(2, count);
    }
    
    @Test
    @DisplayName("Поиск по названию")
    void testFindByName() {
        // Arrange
        Painting painting = new Painting("Звёздная ночь", testArtist, testCategory, "масло", 74, 92);
        repository.save(painting);
        
        // Act
        List<MuseumItem> results = repository.findByName("Звёздная ночь");
        
        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(item -> item.getName().equals("Звёздная ночь")));
    }
    
    @Test
    @DisplayName("Поиск по инвентарному номеру")
    void testFindByInventoryNumber() {
        // Arrange - инвентарный номер устанавливается вручную для теста
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        painting.setInventoryNumber("МУЗ-ТЕСТ-2025-001");
        MuseumItem saved = repository.save(painting);
        String inventoryNumber = saved.getInventoryNumber();
        
        // Act
        MuseumItem found = repository.findByInventoryNumber(inventoryNumber).orElse(null);
        
        // Assert
        assertNotNull(found);
        assertEquals(inventoryNumber, found.getInventoryNumber());
    }
    
    @Test
    @DisplayName("Проверка наличия инвентарного номера")
    void testExistsByInventoryNumber() {
        // Arrange - инвентарный номер устанавливается вручную для теста
        Painting painting = new Painting("Картина", testArtist, testCategory, "масло", 74, 92);
        painting.setInventoryNumber("МУЗ-ТЕСТ-2025-002");
        MuseumItem saved = repository.save(painting);
        String inventoryNumber = saved.getInventoryNumber();
        
        // Act
        boolean exists = repository.existsByInventoryNumber(inventoryNumber);
        
        // Assert
        assertTrue(exists);
    }
    
    @Test
    @DisplayName("Удаление всех экспонатов")
    void testDeleteAll() {
        // Arrange
        Painting painting1 = new Painting("Картина 1", testArtist, testCategory, "масло", 74, 92);
        Painting painting2 = new Painting("Картина 2", testArtist, testCategory, "масло", 80, 100);
        
        repository.save(painting1);
        repository.save(painting2);
        
        // Act
        repository.deleteAll();
        List<MuseumItem> all = repository.findAll();
        
        // Assert
        assertTrue(all.isEmpty());
    }
}
