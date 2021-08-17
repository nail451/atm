package Bank;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Класс описывающий обработку запросов класса банк к базе данных
 */
public class BankController{

    private Connection dbConnection;

    /**
     * Конструктор класса банк, при инициализации создает подключениен к базе данных.
     */
    public BankController() {
        FileInputStream fis;
        Properties property = new Properties();

        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            property.load(fis);
            String host = property.getProperty("db.host");
            String login = property.getProperty("db.login");
            String password = property.getProperty("db.password");
            dbConnection = DriverManager.getConnection(host, login, password);
        } catch (IOException e) {
            System.err.println("ОШИБКА: Конфигурационный файл отсуствует.");
        } catch (SQLException throwables) {
            System.err.println("ОШИБКА: Не удалось подключится к базе данных.");
        }
    }

    public boolean checkCardData(String pinCode, String accountNumber, String expirationDate, String holderName) throws SQLException {
        String query =
                "SELECT " +
                    "count(*) " +
                "FROM " +
                    "clients cl, plastic_cards crd, accounts acs " +
                "WHERE " +
                    "crd.account_id = acs.id AND crd.client_id = cl.id AND acs.client_id = cl.id AND " +
                    "crd.pin = ? AND crd.account_number = ? AND crd.name = ? AND crd.expiration_date = ?";
        PreparedStatement statement = dbConnection.prepareStatement(query);
        statement.setString(1, pinCode);
        statement.setString(2, accountNumber);
        statement.setString(3, holderName);
        statement.setString(4, expirationDate);
        ResultSet resultSet = statement.executeQuery();
        boolean response = false;
        while (resultSet.next()){
            if(resultSet.getInt(1) == 1) response = true;
        }
        return response;
    }

    /**
     * Метод проверки пин кода введенного клиентом.
     * Метод принимает пин код в формате sha256 дергает из базы соль текущего клиента
     * и используя соль генерирует новых хэш и если этот хэш соотвествует указанному в базе
     * возвращает true
     * @param pinCode String строка в формате sha256
     * @param accountNumber String номер карты
     * @param expirationDate String дата окончания поддержки карты
     * @param holderName String имя владельца
     * @return boolean
     */
    public Map<String, String> getUserByCardData(String pinCode, String accountNumber, String expirationDate, String holderName) throws SQLException {
        String query =
                "SELECT " +
                    "cl.name, cl.soname, acs.balance, crd.card_status, acs.account_status, crd.expiration_date " +
                "FROM " +
                    "clients cl, plastic_cards crd, accounts acs " +
                "WHERE " +
                    "crd.account_id = acs.id AND crd.client_id = cl.id AND acs.client_id = cl.id AND " +
                    "crd.pin = ? AND crd.account_number = ? AND crd.name = ? AND crd.expiration_date = ?";
        PreparedStatement statement = dbConnection.prepareStatement(query);
        statement.setString(1, pinCode);
        statement.setString(2, accountNumber);
        statement.setString(3, holderName);
        statement.setString(4, expirationDate);
        System.out.println(pinCode + "|" + accountNumber + "|" + expirationDate + "|" + holderName);
        ResultSet resultSet = statement.executeQuery();
        Map<String, String> response = new HashMap<>();
        java.util.Date today = new java.util.Date();
        java.sql.Timestamp nowTimeStamp = new java.sql.Timestamp(today.getTime());
        while (resultSet.next()){
            if(!resultSet.getString(4).equals("open")) {
                response.put("status", "card on_hold");
            } else if(!resultSet.getString(5).equals("open")) {
                response.put("status", "account on_hold");
            } else if(resultSet.getLong(6) > nowTimeStamp.getTime()){
                response.put("status", "expired");
            } else {
                response.put("status", "ok");
            }
            response.put("name", resultSet.getString(1) + " " + resultSet.getString(2));
            response.put("balance", resultSet.getString(3));
        }
        System.out.println(response);
        return response;
    }
}