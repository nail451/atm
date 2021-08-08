package Bank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
//import org.json.*;

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
        String greeting = in.readLine();
        if ("hello server".equals(greeting)) {
            out.println("hello client");
        }
        else {
            out.println("unrecognised greeting");
        }
    }

    public void serverStop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

/*    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }*/

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
