package kz.enu.museum.util;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Утилита для генерации уникальных инвентарных номеров.
 * Формат: МУЗ-{код категории}-{год}-{порядковый номер}
 * Пример: МУЗ-ПЛ-2025-001
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class InventoryNumberGenerator {
    
    private static final String PREFIX = "МУЗ";
    private static final AtomicLong counter = new AtomicLong(1);
    
    /**
     * Генерирует уникальный инвентарный номер.
     *
     * @param categoryCode код категории (например, ПЛ для Живопись)
     * @return сгенерированный инвентарный номер
     */
    public static String generateInventoryNumber(String categoryCode) {
        int currentYear = Year.now().getValue();
        long number = counter.getAndIncrement();
        
        return String.format("%s-%s-%d-%06d", 
            PREFIX, 
            categoryCode != null ? categoryCode : "ОП",
            currentYear,
            number
        );
    }
    
    /**
     * Генерирует инвентарный номер для конкретного года.
     *
     * @param categoryCode код категории
     * @param year год
     * @return сгенерированный инвентарный номер
     */
    public static String generateInventoryNumber(String categoryCode, int year) {
        long number = counter.getAndIncrement();
        
        return String.format("%s-%s-%d-%06d", 
            PREFIX, 
            categoryCode != null ? categoryCode : "ОП",
            year,
            number
        );
    }
    
    /**
     * Парсит инвентарный номер и извлекает информацию.
     *
     * @param inventoryNumber инвентарный номер
     * @return массив с компонентами [prefix, categoryCode, year, number]
     */
    public static String[] parseInventoryNumber(String inventoryNumber) {
        if (inventoryNumber == null || !inventoryNumber.contains("-")) {
            return new String[]{"", "", "", ""};
        }
        
        return inventoryNumber.split("-");
    }
    
    /**
     * Возвращает текущее значение счётчика.
     *
     * @return текущее значение счётчика
     */
    public static long getCurrentCounter() {
        return counter.get();
    }
    
    /**
     * Устанавливает значение счётчика (для загрузки из БД).
     *
     * @param value новое значение счётчика
     */
    public static void setCounter(long value) {
        counter.set(value);
    }
    
    /**
     * Сбрасывает счётчик на начальное значение.
     */
    public static void reset() {
        counter.set(1);
    }
}
