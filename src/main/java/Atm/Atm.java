package Atm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import Card.Card;
import Interface.Command;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Atm {

    private boolean operationResult;
    private String operationResultDescription;
    private String date;
    private SocketClient connection;

    public Atm() {
    }

    private void sendMsgToTheBank(String msg) {
        connection = new SocketClient();
        try {
            connection.startConnection("127.0.0.1", 451);
            String response = connection.sendMessage(msg);
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("Произошла ошибка подключения, программа будет остановленна");
            System.out.println(e);
            System.exit(0);
        }
    }

    public void checkPin(String enteredPinCode, Card cardForCheck) {
        if(isPinValid(enteredPinCode)) {
            String hashedPinCode = Hashing.sha256()
                    .hashString(enteredPinCode, StandardCharsets.UTF_8)
                    .toString();
            String[] keys = new String[5];
            keys[0] = "0";
            keys[1] = "pin";
            keys[2] = "card_number";
            keys[3] = "expiration_date";
            keys[4] = "name";
            String[] values = new String[5];
            values[0] = "check_pin";
            values[1] = hashedPinCode + "";
            values[2] = cardForCheck.getAccountNumber() + "";
            values[3] = cardForCheck.getExpirationDate() + "";
            values[4] = cardForCheck.getAccountHolderName() + "";
            String json = makeJson(keys,values);
            sendMsgToTheBank(json);
        }
    }

    private boolean isPinValid(String pin) {
        if(pin.length() != 4){
            return false;
        }
        for (int i = 0; i < pin.length(); i++) {
            if(!Character.isDigit(pin.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String makeJson(String[] key, String[] value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Map<String, String> toJson = new HashMap<String, String>();
        if(key.length == value.length) {
            for(int i=0; i< key.length; i++) {
                toJson.put(key[i], value[i]);
            }
        }

        return gson.toJson(toJson);
    }

    public void getDataFromBankAccount() {
    }

    public void getMoneyFromBankAccount() {
    }

    public void payChecks() {
    }

    public void checkAvailableBills() {
    }

    public void billsDispensing() {
    }

    public void putBillsOnBankAccount() {
    }

    public void checkCardExpiration(){
    }

}

class SocketClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}