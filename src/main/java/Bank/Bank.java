package Bank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import Bank.Commands.Command;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.SQLException;
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

    public ServerSocket getServerSocket() { return serverSocket; }

    public void setServerSocket(ServerSocket serverSocket) { this.serverSocket = serverSocket; }

    public Socket getClientSocket() { return clientSocket; }

    public void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }

    public PrintWriter getOut() { return out; }

    public void setOut(PrintWriter out) { this.out = out; }

    public BufferedReader getIn() { return in; }

    public void setIn(BufferedReader in) { this.in = in; }

    public static BankController getBankController() { return bankController; }

    public static void setBankController(BankController bankController) { Bank.bankController = bankController; }



    /**
     * Инициализация сокет сервера
     * @param args стандартное описание метода main
     * @throws IOException, SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        Bank socketServer = new Bank();
        setBankController(BankController.getInstance());
        socketServer.serverStart(451);
    }

    /**
     * Метод запуска сервера и обработки приходящей информации
     * @param listeningPort int порт который будет слушать сервер
     * @throws IOException ошибка ввода вывода
     * @throws SQLException ошибка sql
     */
    public void serverStart(int listeningPort) throws IOException, SQLException {
        setServerSocket(new ServerSocket(listeningPort));
        setClientSocket(getServerSocket().accept());
        setOut(new PrintWriter(getClientSocket().getOutputStream(), true));
        setIn(new BufferedReader(new InputStreamReader(getClientSocket().getInputStream())));
        String request = getIn().readLine();
        if(isJsonValid(request)) {
            Gson gson = new Gson();
            Map<String, Map<String,String>> result = gson.fromJson(request, Map.class);
            String className = result.get("command").get("command");
            Map<String,String> toClassFields = result.get("toClass");
            getOut().println(makeJson(execute(className, toClassFields)));
        }
        serverStop(); //Коннект все равно рушится, так что подчищаем за собой
        serverStart(listeningPort);  //Запускаем сервер по новой
    }

    /**
     * Метод остановки сервера
     * @throws IOException ошибка ввода вывода
     */
    public void serverStop() throws IOException {
        getIn().close();
        getOut().close();
        getClientSocket().close();
        getServerSocket().close();
    }

    /**
     * Инициализация класса спомошью рефлексии
     * После инстанциации запускает метод интерфейса Command.retrieveResult
     * @return коллекция
     * @throws SQLException
     */
    private Map<String,String> execute(String name, Map<String, String> fields) throws SQLException {
        try {
            int id = 1;
            String className = "Bank.Commands." + name;
            Class clazz = Class.forName(className);
            Class[] params = {int.class, Map.class};
            Command command = (Command) clazz.getConstructor(params).newInstance(id, fields);
            return command.retrieveResult();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Метод формирует json из коллекции для ответа клиенту
     * @param map коллекция <String, String>
     * @return строка формата json
     */
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


}
