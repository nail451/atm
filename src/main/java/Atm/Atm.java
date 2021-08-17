package Atm;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import Card.Card;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Atm {

    private boolean operationResult;
    private String operationResultDescription;
    private String date;
    private CardAcceptor cardAcceptor;

    public Atm(){
        System.out.println("Пожалуйста вставьте карту в картоприемник");
    }

    public void newCard(Card newCard) {
        cardAcceptor = new CardAcceptor(newCard);
    }

    public String getOperationResultDescription() {
        return operationResultDescription;
    }

    public void setOperationResultDescription(String operationResultDescription) {
        this.operationResultDescription = operationResultDescription;
    }



    private String sendDataToTheBank(String data) {
        SocketClient connection = new SocketClient();
        try {
            connection.startConnection("127.0.0.1", 451);
            return connection.sendMessage(data);
        } catch (IOException e) {
            System.out.println("Произошла ошибка подключения, программа будет остановленна");
            System.out.println(e);
            System.exit(0);
            return "";
        }
    }

    /**
     * Метод прооверки соотвествия введеного пинкода сохраненному в банке
     * Собирает два массива
     * 1. Массив с ключами где 0 элемент = "command"
     * 2. Массив со значениями где 0 элемент = "check_pin"
     * Отправлет массивы на преобразование в json
     * Отправлет json на сервер
     * @param enteredPinCode строка с введенным пин кодом
     */
    public String checkPin(String enteredPinCode){
        if(cardAcceptor.getCardInside()) {
            if (isPinValid(enteredPinCode)) {
                String hashedPinCode = Hashing.sha256()
                        .hashString(enteredPinCode, StandardCharsets.UTF_8)
                        .toString();
                String[] keys = new String[5];
                keys[0] = "command";
                keys[1] = "pin";
                keys[2] = "card_number";
                keys[3] = "expiration_date";
                keys[4] = "name";
                String[] values = new String[5];
                values[0] = "check_pin";
                values[1] = hashedPinCode + "";
                values[2] = cardAcceptor.getAccountNumber() + "";
                values[3] = cardAcceptor.getExpirationDate() + "";
                values[4] = cardAcceptor.getHolder() + "";
                String json = makeJson(keys, values);
                return sendDataToTheBank(json);
            } else {
                return "Пинкод не корректен";
            }
        } else {
            return "Нет карты";
        }
    }

    /**
     * Метод проверки валидности введенного пользователем пин кода
     * @param pin введенная строка
     * @return boolean
     */
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

    /**
     * Метод формирующий json строку по средствам двух массивов с одинаковой длинной
     * @param key Массив ключей
     * @param value Масссив значений
     * @return String формата json
     */
    private String makeJson(String[] key, String[] value) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Map<String, String> toJson = new HashMap<>();
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

    public void eject(){
        cardAcceptor.cardEject();
    }

}

class BillAcceptor implements Receiving {
    private String statusOfBillAcceptor;
    private boolean isBillAcceptorOpen;
    private int[][] BillAcceptorContains;

    public void openBillAcceptor(){
    }

    public void closeBillAcceptor(){
    }

    @Override
    public void receive() {
    }

    public void checkNewAcceptorContains(){
    }

}

class CardAcceptor implements Receiving {
    private Card insertedCard;
    private boolean cardInside;
    private boolean cardValide;

    private String accountNumber;
    private String expirationDate;
    private String holder;

    public CardAcceptor(Card newCard) {
        insertedCard = newCard;
        receive();
        if(isValidCard(newCard)) {
            cardInside = true;
            cardValide = true;
        } else {
            cardValide = false;
        }
    }

    public boolean getCardInside() {
        return cardInside;
    }

    private void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber(){
        return accountNumber;
    }

    private void setExpirationDate(String expirationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(expirationDate);
            this.expirationDate = parsedDate.getTime()/1000 + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getExpirationDate(){
        return expirationDate;
    }

    private void setHolder(String holder) {
        this.holder = holder;
    }

    public String getHolder(){
        return holder;
    }

    @Override
    public void receive() {
        assert insertedCard != null;
        List<String> insertedCardData = insertedCard.getCardData();
        for(String line : insertedCardData) {
            if (line.length() == 16 && isNumeric(line)) setAccountNumber(line);
            if (line.contains("/")) setExpirationDate(line);
            if (line.contains(" ")) setHolder(line);
        }
    }

    public void cardEject(){
        insertedCard = null;
        cardInside = false;
    }

    private boolean isValidCard(Card newCard){
        boolean isValid = false;
        if(Objects.nonNull(accountNumber) && isNumeric(accountNumber) && accountNumber.length() == 16) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int strSize = str.length();
        for (int i = 0; i < strSize; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

class Dispenser implements Issuing {
    private boolean billsInTheDispenser;
    private short billsInTheDispenserCount;
    private HashMap<Integer, Integer> arrayOfBillsInTheDispenser = new HashMap<>();
}

class Printer implements Issuing {
    private int receiptPaper;
    private boolean receiptPaperError;

    public void createNewReceipt(){
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