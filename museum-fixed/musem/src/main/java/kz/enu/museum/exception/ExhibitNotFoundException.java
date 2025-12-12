package kz.enu.museum.exception;

/**
 * Исключение, выбрасываемое когда экспонат не найден.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class ExhibitNotFoundException extends MuseumException {
    
    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message описание ошибки
     */
    public ExhibitNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Конструктор с сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause исходное исключение
     */
    public ExhibitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
