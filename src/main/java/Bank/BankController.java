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

    private Map<String, Connection> connectionMap = new HashMap<>();
    private Map<String, Savepoint> savePointMap = new HashMap<>();
    private Map<String, PreparedStatement> statementMap = new HashMap<>();

    public Map<String, Connection> getConnectionMap() { return connectionMap; }

    public Map<String, Savepoint> getSavePointMap() { return savePointMap; }

    public Map<String, PreparedStatement> getStatementMap() { return statementMap; }

    public void setConnectionMap(String hash, Connection dbConnection) { this.connectionMap.put(hash, dbConnection); }

    public void setSavePointMap(String hash, Savepoint savepoint) { this.savePointMap.put(hash, savepoint); }

    public void setStatementMap(String hash, PreparedStatement statement) { this.statementMap.put(hash, statement); }

    public void removeFromConnectionMap(String hash) { this.connectionMap.remove(hash); }

    public void removeFromSavePointMap(String hash) { this.savePointMap.remove(hash); }

    public void removeFromStatementMap(String hash) { this.statementMap.remove(hash); }

    /**
     * Метод создания подключения к базе
     * Подключение без авто комита, так что
     * для завершения транзации используем commit
     * Реквизиты к базе данных указываются в файле
     * resources/config.properties
     */
    public Connection createNewConnection() {
        FileInputStream fis;
        Properties property = new Properties();
        Connection dbConnection = null;
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
        return dbConnection;
    }

    /**
     * Метод закрывающий подключение к базе
     */
    private void closeConnection(Connection dbConnection, Statement statement, ResultSet resultSet) {
        try {
            if(dbConnection != null) dbConnection.close();
            if(statement != null) statement.close();
            if(resultSet != null) resultSet.close();
        } catch (SQLException q) {
            System.err.println("ОШИБКА: Не удалось закрыть подключение к базе.");
        }
    }

    /**
     * Метод закрывающий подключение к базе, если в коннекте не использовался селект
     */
    private void closeNoResultConnection(Connection dbConnection, Statement statement) {
        try {
            if(dbConnection != null) dbConnection.close();
            if(statement != null) statement.close();
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
        Connection dbConnection = createNewConnection();
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
        closeConnection(dbConnection, statement, resultSet);
        return response;
    }

    /**
     * Метод общей проверки данных о клиенте, карте и счете.
     * @param accountNumber String номер карты
     * @return коллекция hashmap с ключами status, name, balance
     */
    public Map<String, String> getUserByCardData(String accountNumber) throws SQLException {
        Connection dbConnection = createNewConnection();
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
        closeConnection(dbConnection, statement, resultSet);
        return response;
    }

    public Map<String, String> addSomeMoneyToTheBankAccount(String atmHash, String cardNumber, String sum, int action) {
        Connection dbConnection;
        Savepoint savepoint;
        PreparedStatement statement;
        Map<String, String> response = new HashMap<>();
        try {
            switch (action) {
                case 1 -> {
                    if(getStatementMap().containsKey(atmHash)){
                        dbConnection = getConnectionMap().get(atmHash);
                        savepoint = getSavePointMap().get(atmHash);
                        dbConnection.rollback(savepoint);
                    } else {
                        dbConnection = createNewConnection();
                        savepoint = dbConnection.setSavepoint("savepoint");
                        setConnectionMap(atmHash, dbConnection);
                        setSavePointMap(atmHash, savepoint);
                    }
                    statement = makeAddMoneyQuery(dbConnection, sum, cardNumber);
                    statement.executeUpdate();
                    setStatementMap(atmHash, statement);
                }
                case 2 -> {
                    dbConnection = getConnectionMap().get(atmHash);
                    savepoint = getSavePointMap().get(atmHash);
                    statement = getStatementMap().get(atmHash);
                    dbConnection.rollback(savepoint);
                    closeNoResultConnection(dbConnection, statement);
                    removeFromConnectionMap(atmHash);
                    removeFromSavePointMap(atmHash);
                    removeFromStatementMap(atmHash);
                }
                case 3 -> {
                    dbConnection = getConnectionMap().get(atmHash);
                    savepoint = getSavePointMap().get(atmHash);
                    statement = getStatementMap().get(atmHash);
                    dbConnection.commit();
                    dbConnection.releaseSavepoint(savepoint);
                    closeNoResultConnection(dbConnection, statement);
                    removeFromConnectionMap(atmHash);
                    removeFromSavePointMap(atmHash);
                    removeFromStatementMap(atmHash);
                }
                default -> throw new IllegalStateException("Unexpected value: " + action);
            }
            response.put("status", "true");
        } catch (SQLException s) {
            response.put("status", "false");
        }
        return response;
    }

    private PreparedStatement makeAddMoneyQuery(Connection dbConnection, String sum, String cardNumber) throws SQLException {
        String query =
                "UPDATE " +
                    "accounts acc, plastic_cards pc " +
                "SET " +
                    "acc.balance = acc.balance + ? " +
                "WHERE acc.id = pc.account_id AND pc.account_number = ?";
        PreparedStatement statement = dbConnection.prepareStatement(query);
        statement.setString(1, sum);
        statement.setString(2, cardNumber);
        return statement;
    }
}