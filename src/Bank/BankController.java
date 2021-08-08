package Bank;

/**
 * Класс описывающий обработку запросов класса банк к базе данных
 */
public class BankController{

    //TODO: заменить стринг на обект дб конекта
    String dbConnection;

    /**
     * Конструктор класса банк, при инициализации создает подключениен к базе данных.
     */
    public BankController() {

    }

    /**
     * Метод проверки пин кода введенного клиентом.
     * Метод принимает пин код в формате sha256 дергает из базы соль текущего клиента
     * и используя соль генерирует новых хэш и если этот хэш соотвествует указанному в базе
     * возвращает true
     * @param hashedPinCode String строка в формате sha256
     * @return boolean
     */
    public boolean checkPinCode (String hashedPinCode) {
        boolean pinCheckResult = false;
        return pinCheckResult;
    }
}
