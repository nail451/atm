package Bank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import Interface.Command;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;


/**
 * Класс описывающий сущность Банк, для работы с atm
 */
public class Bank {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Инициализация сокет сервера
     * @param args стандартное описание метода main
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Bank socketServer = new Bank();
        socketServer.serverStart(451);
    }

    public void serverStart(int listeningPort) throws IOException {
        serverSocket = new ServerSocket(listeningPort);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String request = in.readLine();
        if(isJsonValid(request)){
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Commands result = gson.fromJson(request, Commands.class);
            System.out.println(request);
            System.out.println(result.toString());
        }


        switch (request) {
            case "hello": {
                System.out.println("hello");
                out.println("hello");
                break;
            }
            case "pin": {
                //pinCheck(pinCode);
                break;
            }
            default: {
                System.out.println("unrecognised greeting");
                out.println("unrecognised greeting");
                break;
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
    public boolean pinCheck(int pinCode) {
        boolean checkResult = false;
        return checkResult;
    }

}

class Commands implements Command {
    private String key;
    private String value;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ": " + value;
    }
}
