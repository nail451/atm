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
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, SQLException {
        Bank socketServer = new Bank();
        bankController = new BankController();
        socketServer.serverStart(451);
    }

    public void serverStart(int listeningPort) throws IOException, SQLException {
        serverSocket = new ServerSocket(listeningPort);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String request = in.readLine();
        if(isJsonValid(request)) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Map<String, String> result = gson.fromJson(request, HashMap.class);
            switch (result.get("0")) {
                case "check_connection": {
                    System.out.println("hello");
                    out.println("hello");
                    break;
                }
                case "check_pin": {
                    out.println(pinCheck(result.get("pin"), result.get("card_number"), result.get("expiration_date"), result.get("name")));
                    break;
                }
                default: {
                    System.out.println("unrecognised greeting");
                    out.println("unrecognised greeting");
                    break;
                }
            }
        }
        serverStop(); //Коннект все равно рушится, так что подчищаем за собой
        serverStart(listeningPort);  //Запускаем сервер по новой
    }

    public void serverStop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    private boolean isJsonValid(String json) {
        Gson gson = new Gson();
        String gsonClass = String.valueOf(gson.fromJson(json, Object.class).getClass());
        String equalsString = "class com.google.gson.internal.LinkedTreeMap";
        return gsonClass.equals(equalsString);
    }


    /**
     * Метод проверки пин кода введенного клиентом
     * @param pinCode
     * @return
     */
    public boolean pinCheck(String pinCode, String accountNumber, String expirationDate, String holderName) throws SQLException {
        return bankController.getUserByCardData(pinCode, accountNumber, expirationDate, holderName);
    }

}
