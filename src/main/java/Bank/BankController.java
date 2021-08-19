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
     * Метод создания подключения к базе
     * Подключение без авто комита, так что
     * для завершения транзации используем commit
     * Реквизиты к базе данных указываются в файле
     * resources/config.properties
     */
    private void createNewConnection() {
        FileInputStream fis;
        Properties property = new Properties();

        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            property.load(fis);
            String host = property.getProperty("db.host");
            String login = property.getProperty("db.login");
            String password = property.getProperty("db.password");
            dbConnection = DriverManager.getConnection(host, login, password);
            dbConnection.setAutoCommit(false);
        } catch (IOException e) {
            System.err.println("ОШИБКА: Конфигурационный файл отсуствует.");
        } catch (SQLException q) {
            System.err.println("ОШИБКА: Не удалось подключится к базе данных.");
        }
    }

    /**
     * Метод закрывающий подключение к базе
     */
    private void closeConnection(Statement statement, ResultSet resultSet) {
        try {
            this.dbConnection.close();
            statement.close();
            resultSet.close();
        } catch (SQLException q) {
            System.err.println("ОШИБКА: Не удалось закрыть подключение к базе.");
        }

    }

    /**
     * Метод проверки pin кода и данных полученных с карты клиента
     * @param pinCode пин код в формате sha256
     * @param accountNumber номер карты
     * @param expirationDate дота истечения срока действия
     * @param holderName имя и фамилия держателя карты в транскрипции
     * @return boolean
     * @throws SQLException ошибка sql
     */
    public Map<String, String> checkCardData(String pinCode, String accountNumber, String expirationDate, String holderName) throws SQLException {
        createNewConnection();
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

        Map<String, String> response = new HashMap<>();
        while (resultSet.next()){
            if(resultSet.getInt(1) == 1) {
                response.put("status","true");
            }
            else {
                response.put("status","false");
            }
        }
        closeConnection(statement, resultSet);
        return response;
    }

    /**
     * Метод общей проверки данных о клиенте, карте и счете.
     * @param accountNumber String номер карты
     * @return коллекция hashmap с ключами status, name, balance
     */
    public Map<String, String> getUserByCardData(String accountNumber) throws SQLException {
        createNewConnection();
        String query =
                "SELECT " +
                    "cl.name, cl.soname, acs.balance, crd.card_status, acs.account_status, crd.expiration_date " +
                "FROM " +
                    "clients cl, plastic_cards crd, accounts acs " +
                "WHERE " +
                    "crd.account_id = acs.id AND crd.client_id = cl.id AND acs.client_id = cl.id AND " +
                    "crd.account_number = ?";
        PreparedStatement statement = dbConnection.prepareStatement(query);
        statement.setString(1, accountNumber);
        ResultSet resultSet = statement.executeQuery();
        Map<String, String> response = new HashMap<>();
        java.util.Date today = new java.util.Date();
        java.sql.Timestamp nowTimeStamp = new java.sql.Timestamp(today.getTime());
        while (resultSet.next()){
            if(!resultSet.getString(4).equals("open")) {
                response.put("status", "card_on_hold");
            } else if(!resultSet.getString(5).equals("open")) {
                response.put("status", "account_on_hold");
            } else if(resultSet.getLong(6) > nowTimeStamp.getTime()){
                response.put("status", "expired");
            } else {
                response.put("status", "ok");
            }
            response.put("name", resultSet.getString(1) + " " + resultSet.getString(2));
            response.put("balance", resultSet.getString(3));
        }
        closeConnection(statement, resultSet);
        return response;
    }
}