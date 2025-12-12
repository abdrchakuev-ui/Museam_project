package kz.enu.museum.exception;

/**
 * Исключение, выбрасываемое при попытке дублирования инвентарного номера.
 *
 * @author Есим Артём
 * @version 1.0
 * @since 2025
 */
public class DuplicateInventoryNumberException extends MuseumException {
    
    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message описание ошибки
     */
    public DuplicateInventoryNumberException(String message) {
        super(message);
    }
    
    /**
     * Конструктор с сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause исходное исключение
     */
    public DuplicateInventoryNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
