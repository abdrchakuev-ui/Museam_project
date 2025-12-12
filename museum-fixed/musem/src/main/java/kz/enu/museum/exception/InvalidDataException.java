package kz.enu.museum.exception;

/**
 * Исключение, выбрасываемое при некорректных данных.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class InvalidDataException extends MuseumException {
    
    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message описание ошибки
     */
    public InvalidDataException(String message) {
        super(message);
    }
    
    /**
     * Конструктор с сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause исходное исключение
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
