package Bank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



/**
 * Класс описывающий сущность Банк, для работы с atm
 */
public class Bank {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private static BankController bankController;

    /**
     * Инициализация сокет сервера
     * @param args стандартное описание метода main
     * @throws IOException, SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        Bank socketServer = new Bank();
        bankController = new BankController();
        socketServer.serverStart(451);
    }

    /**
     * Метод запуска сервера и обработки приходящей информации
     * @param listeningPort int порт который будет слушать сервер
     * @throws IOException ошибка ввода вывода
     * @throws SQLException ошибка sql
     */
    public void serverStart(int listeningPort) throws IOException, SQLException {
        serverSocket = new ServerSocket(listeningPort);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String request = in.readLine();
        if(isJsonValid(request)) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            HashMap<String, String> result = gson.fromJson(request, HashMap.class);
            switch (result.get("command")) {
                case "check_connection" -> {
                    System.out.println("hello");
                    out.println("hello");
                }
                case "check_pin" -> out.println(
                        makeJson(
                                pinCheck(
                                        result.get("pin"),
                                        result.get("card_number"),
                                        result.get("expiration_date"),
                                        result.get("name"))));
                case "get_data" -> out.println(
                        makeJson(
                                getUserData(
                                        result.get("card_number"))));
                default -> {
                    System.out.println("unrecognised greeting");
                    out.println("unrecognised greeting");
                }
            }
        }
        serverStop(); //Коннект все равно рушится, так что подчищаем за собой
        serverStart(listeningPort);  //Запускаем сервер по новой
    }

    /**
     * Метод остановки сервера
     * @throws IOException ошибка ввода вывода
     */
    public void serverStop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    private String makeJson(Map<String, String> map) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.toJson(map);
    }

    /**
     * Проверка пришедшей на сервер строки на json
     * @param json строка в вформате json
     * @return boolean
     */
    private boolean isJsonValid(String json) {
        Gson gson = new Gson();
        String gsonClass = String.valueOf(gson.fromJson(json, Object.class).getClass());
        String equalsString = "class com.google.gson.internal.LinkedTreeMap";
        return gsonClass.equals(equalsString);
    }


    /**
     * @param pinCode пин код
     * @param accountNumber номер карты
     * @param expirationDate дата валидности в unixtime
     * @param holderName Имя и Фамилия владельца
     * @return HashMap
     * @throws SQLException ошибка sql
     */
    public Map<String, String> pinCheck(String pinCode, String accountNumber, String expirationDate, String holderName) throws SQLException {
        return bankController.checkCardData(pinCode, accountNumber, expirationDate, holderName);
    }

    /**
     * @param accountNumber номер карты
     * @return HashMap
     * @throws SQLException ошибка sql
     */
    public Map<String, String> getUserData(String accountNumber) throws SQLException {
        return bankController.getUserByCardData(accountNumber);
    }

}
