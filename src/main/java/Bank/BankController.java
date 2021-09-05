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

    private static BankController instance;
    private Map<String, Connection> connectionMap = new HashMap<>();
    private Map<String, Savepoint> savePointMap = new HashMap<>();
    private Map<String, PreparedStatement> statementMap = new HashMap<>();

    private Map<String, Connection> getConnectionMap() { return connectionMap; }

    private Map<String, Savepoint> getSavePointMap() { return savePointMap; }

    private Map<String, PreparedStatement> getStatementMap() { return statementMap; }

    private void setConnectionMap(String hash, Connection dbConnection) { this.connectionMap.put(hash, dbConnection); }

    private void setSavePointMap(String hash, Savepoint savepoint) { this.savePointMap.put(hash, savepoint); }

    private void setStatementMap(String hash, PreparedStatement statement) { this.statementMap.put(hash, statement); }

    private void removeFromConnectionMap(String hash) { this.connectionMap.remove(hash); }

    private void removeFromSavePointMap(String hash) { this.savePointMap.remove(hash); }

    private void removeFromStatementMap(String hash) { this.statementMap.remove(hash); }

    /**
     * Приватный конеструктор класса, как часть singleton
     */
    private BankController(){
    }

    /**
     * Реализация шаблона singleton, создаем instance только в том случае если его нет
     * @return instance
     */
    public static BankController getInstance() {
        if (instance == null) {
            instance = new BankController();
        }
        return instance;
    }

    /**
     * Метод создания подключения к базе
     * Подключение без авто комита, так что
     * для завершения транзации используем commit
     * Реквизиты к базе данных указываются в файле
     * resources/config.properties
     */
    public Connection createNewConnection(boolean autoCommit) {
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
            dbConnection.setAutoCommit(autoCommit);
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
    public Map<String, String> checkCardData(String pinCode, String accountNumber, String expirationDate, String holderName, boolean autoCommit) throws SQLException {
        Connection dbConnection = createNewConnection(autoCommit);
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
    public Map<String, String> getUserByCardData(String accountNumber, boolean autoCommit) throws SQLException {
        Connection dbConnection = createNewConnection(autoCommit);
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
            boolean error = false;
            if(!resultSet.getString(4).equals("open")) {
                response.put("status", "card_on_hold");
                error = true;
            }
            if(!resultSet.getString(5).equals("open")) {
                response.put("status", "account_on_hold");
                error = true;
            }
            if(resultSet.getLong(6) < nowTimeStamp.getTime()/1000){
                response.put("status", "expired");
                error = true;
            }
            if(!error) {
                response.put("status", "ok");
            }
            response.put("name", resultSet.getString(1) + " " + resultSet.getString(2));
            response.put("balance", resultSet.getString(3));
        }
        closeConnection(dbConnection, statement, resultSet);
        return response;
    }

    public Map<String, String> makeTransaction(String atmHash, PreparedStatement preparedStatement) {
        Map<String, String> response = new HashMap<>();
        try {
            preparedStatement.executeUpdate();
            setStatementMap(atmHash, preparedStatement);
            response.put("status", "true");
        } catch (SQLException s) {
            response.put("status", "false");
        }
        return response;
    }

    private void setTransaction(String atmHash) throws SQLException {
        Connection dbConnection;
        Savepoint savepoint;
        if(!getStatementMap().containsKey(atmHash)){
            dbConnection = createNewConnection(false);
            savepoint = dbConnection.setSavepoint("savepoint");
            setConnectionMap(atmHash, dbConnection);
            setSavePointMap(atmHash, savepoint);
        }
    }

    public Map<String, String> finalTransaction(String atmHash, boolean isCommit) {
        Connection dbConnection = getConnectionMap().get(atmHash);
        Savepoint savepoint = getSavePointMap().get(atmHash);
        PreparedStatement statement = getStatementMap().get(atmHash);
        Map<String, String> response = new HashMap<>();
        try {
            if(isCommit) {
                dbConnection.commit();
                dbConnection.releaseSavepoint(savepoint);
            } else {
                dbConnection.rollback(savepoint);
            }
            response.put("status", "true");
        } catch (SQLException s) {
            response.put("status", "false");
        }
        closeNoResultConnection(dbConnection, statement);
        removeFromConnectionMap(atmHash);
        removeFromSavePointMap(atmHash);
        removeFromStatementMap(atmHash);
        return response;
    }

    /**
     * Оформление и подготовка запроса на снятие денег со счета
     * @param atmHash хэш банкомата
     * @param sum сумма которую собираемся списывать
     * @param cardNumber номер карты
     * @return PreparedStatement подготовленный запрос
     * @throws SQLException при возникновении ошибки банкомат получит status false
     */
    public PreparedStatement makeMoneyQuery(String atmHash, String sum, String cardNumber, String getOrPut) throws SQLException {
        setTransaction(atmHash);
        String math;
        if(getOrPut.equals("put")) math = "+";
        else math = "-";
        Connection dbConnection = getConnectionMap().get(atmHash);
        String query =
                "UPDATE " +
                    "accounts acc, plastic_cards pc " +
                "SET " +
                    "acc.balance = acc.balance " + math + " ? " +
                "WHERE acc.id = pc.account_id AND pc.account_number = ?";
        PreparedStatement statement = dbConnection.prepareStatement(query);
        statement.setString(1, sum);
        statement.setString(2, cardNumber);
        return statement;
    }
}