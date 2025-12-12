package kz.enu.museum.exception;

/**
 * Исключение для ошибок загрузки данных из файлов.
 * Выбрасывается при возникновении проблем с чтением или десериализацией JSON файлов.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class DataLoadException extends MuseumException {
    
    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public DataLoadException(String message) {
        super(message);
    }
    
    /**
     * Конструктор с сообщением об ошибке и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
