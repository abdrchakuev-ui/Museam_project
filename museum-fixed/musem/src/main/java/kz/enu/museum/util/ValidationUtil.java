package kz.enu.museum.util;

/**
 * Утилита для валидации данных приложения.
 * Предоставляет статические методы для проверки корректности различных типов данных.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ValidationUtil {
    
    /**
     * Проверяет, что строка не пустая и не null.
     *
     * @param value значение для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если значение пустое или null
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }
    
    /**
     * Проверяет, что число положительное.
     *
     * @param value значение для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число не положительное
     */
    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " должно быть положительным числом");
        }
    }
    
    /**
     * Проверяет, что число находится в диапазоне.
     *
     * @param value значение для проверки
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число вне диапазона
     */
    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                fieldName + " должно быть между " + min + " и " + max
            );
        }
    }
    
    /**
     * Проверяет корректность электронной почты (базовая проверка).
     *
     * @param email электронная почта для проверки
     * @return true если формат корректен
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Проверяет, что объект не null.
     *
     * @param obj объект для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если объект null
     */
    public static void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }
}
