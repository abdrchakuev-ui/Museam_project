package kz.enu.museum.exception;

/**
 * Базовое исключение для приложения музея.
 * Все исключения приложения наследуют от этого класса.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class MuseumException extends RuntimeException {
    
    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message описание ошибки
     */
    public MuseumException(String message) {
        super(message);
    }
    
    /**
     * Конструктор с сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause исходное исключение
     */
    public MuseumException(String message, Throwable cause) {
        super(message, cause);
    }
}
