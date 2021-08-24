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

/**
 * Основной класс описывающий работу банкомата
 */
public class Atm {

    private String uniqHash;
    private boolean operationCritical = false;
    private boolean operationStatus;
    private String operationResult;
    private int falseCounter = 0;
    private CardAcceptor cardAcceptor;
    private final BillAcceptor billAcceptor = new BillAcceptor();
    private final Dispenser dispenser = new Dispenser();
    private final Printer printer = new Printer();

    /// get+set ///

    public String getUniqHash() { return uniqHash; }

    public void setUniqHash(String uniqHash) { this.uniqHash = uniqHash; }

    public boolean isOperationCritical() { return operationCritical; }

    public boolean getOperationStatus() {
        return operationStatus;
    }

    public String getOperationResult() { return operationResult; }

    public CardAcceptor getCardAcceptor() {
        return cardAcceptor;
    }

    public int getFalseCounter() { return falseCounter; }

    public void setOperationCritical(boolean operationCritical) { this.operationCritical = operationCritical; }

    public void setOperationStatus(boolean operationStatus) { this.operationStatus = operationStatus; }

    public void setOperationResult(String operationResult) {
        this.operationResult = operationResult;
    }

    public void setCardAcceptor(CardAcceptor cardAcceptor) {
        this.cardAcceptor = cardAcceptor;
    }

    public void setFalseCounter(int falseCounter) { this.falseCounter = falseCounter; }

    /// get+set ///

    /**
     * Конеструктор класса
     */
    public Atm(){
        System.out.println("Пожалуйста вставьте карту в картоприемник");
        setUniqHash(generateRandomHash());
    }

    /**
     * Метод объявляет новый экземпляр класса CardAcceptor и передает в него объект класса Card
     * @param newCard объект класса Card
     */
    public void newCard(Card newCard) {
        setCardAcceptor(new CardAcceptor(newCard));
    }

    /**
     * Метод отправки данных на сокет сервер банка
     * @param data json строка с данными
     * @return hashmap с ответом сервера преобразоаваный из json
     */
    private Map<String, String> sendDataToTheBank(String data) throws AtmException {
        SocketClient connection = new SocketClient();
        Map<String, String> response;
        try {
            connection.startConnection("127.0.0.1", 451);
            String result = connection.sendMessage(data);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            response = gson.fromJson(result, HashMap.class);
            connection.stopConnection();
        } catch (IOException e) {
            throw new AtmException("Произошла ошибка подключения", false);
        }
        return response;
    }

    /**
     * Метод прооверки соотвествия введеного пинкода сохраненному в банке
     * Собирает hashmap коллекцию
     * где 0 ключ    = "command"
     * где 0 элемент = "check_pin"
     * Преобразует коллекцию в json
     * Отправлет json на сервер
     * @param enteredPinCode строка с введенным пин кодом
     */
    public void checkPin(String enteredPinCode) throws Exception {
        if(cardAcceptor.getCardInside()) {
            if(getFalseCounter()>=5) {
                System.out.println("\n\n___Слишком много ошибок при вводе пин кода___");
                setOperationCritical(true);
                setFalseCounter(0);
                throw new Exception();
            }
            if (isPinValid(enteredPinCode)) {
                String hashedPinCode = Hashing.sha256()
                        .hashString(enteredPinCode, StandardCharsets.UTF_8)
                        .toString();
                Map<String, String> toJson = new HashMap<>();
                toJson.put("command", "check_pin");
                toJson.put("pin", hashedPinCode + "");
                toJson.put("card_number", cardAcceptor.getAccountNumber());
                toJson.put("expiration_date", cardAcceptor.getExpirationDate());
                toJson.put("name", cardAcceptor.getHolder());
                String json = makeJson(toJson);
                Map<String,String> result = sendDataToTheBank(json);
                if(result.get("status").equals("true")) {
                    setOperationStatus(true);
                    setOperationResult("Принято");
                    setFalseCounter(0);
                } else {
                    setOperationStatus(false);
                    setOperationResult("Пин код не корректен");
                    setFalseCounter(getFalseCounter()+1);
                }
            } else {
                setOperationStatus(false);
                setOperationResult("Пин код не корректен");
                setFalseCounter(getFalseCounter()+1);
                throw new Exception();
            }
        } else {
            setOperationStatus(false);
            setOperationResult("Отсутсвует карта");
            setOperationCritical(true);
            throw new Exception();
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
     * Метод формирующий json строку из hashmap коллекции
     * @param toJson hashmap
     * @return String формата json
     */
    private String makeJson(Map<String, String> toJson) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.toJson(toJson);
    }

    /**
     * Метод генерации 7 символьной строки
     * для унификации запущенного экземпляра класса
     */
    public String generateRandomHash() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 7;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    /**
     * Метод формирует строку на получение данных по карте и отправляет ее на сервер
     * @return
     * @throws AtmException
     */
    private Map<String,String> getDataFromBank() throws AtmException {
        CardAcceptor cardAcceptor = getCardAcceptor();
        Map<String, String> toJson = new HashMap<>();
        toJson.put("command", "get_data");
        toJson.put("card_number", cardAcceptor.getAccountNumber());
        String json = makeJson(toJson);
        return sendDataToTheBank(json);
    }

    /**
     * Метод отправки запроса на сервер банка с получением данных по соотвествующей карте
     * Ответ записывается в
     * operationStatus
     * operationResult
     */
    public void getDataFromBankAccount() throws AtmException {
        Map<String,String> result = getDataFromBank();
        if(result.get("status").equals("ok")) {
            setOperationStatus(true);
            setOperationResult("Здравсвуйте " + result.get("name") + "\nВаш баланс счета: " + result.get("balance"));
        } else {
            switch (result.get("status")) {
                case "card_on_hold" -> {
                    setOperationStatus(false);
                    setOperationResult("Карта заблокирована банком");
                }
                case "account_on_hold" -> {
                    setOperationStatus(false);
                    setOperationResult("Счет привязанный к карте заблокирован");
                }
                case "expired" -> {
                    setOperationStatus(false);
                    setOperationResult("Срок действия карты истек");
                }
            }
        }
    }

    /**
     * Метод формирует строку в которой описывается кол-во доступных купур в банкомате
     */
    public void checkOutputCassette() {
        Map<String,String> billsAvailable = dispenser.checkCassette();
        LinkedList<String> available = new LinkedList<>(billsAvailable.keySet());
        String outputBills = "Купюры в банкомате:\n";
        available.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        for(String i : available) {
            int bill = Integer.parseInt(billsAvailable.get(i));
            if(bill >= 500) outputBills += i + ": много";
            if(bill < 500 && bill >= 100) outputBills += i + ": средне";
            if(bill < 100 && bill > 0) outputBills += i + ": мало";
            if(bill == 0) outputBills += i + ": нет";
            if(!available.getLast().equals(i)) outputBills += ", ";
        }
        System.out.println(outputBills);
    }

    /**
     * Метод запрашивает данные по карте у банка и возвращает баланс
     * @return int баланс счета
     * @throws AtmException
     */
    public int getSumFromBankAccount() throws AtmException {
        Map<String,String> result = getDataFromBank();
        return Integer.parseInt(result.get("balance"));
    }

    private boolean isAllGood(String yesNoAskString) {
        System.out.println(yesNoAskString);
        System.out.println("1.Да 2.Нет");
        Scanner nextMove = new Scanner(System.in);
        if (nextMove.hasNextLine()) {
            String answer = nextMove.nextLine();
            if (isNumeric(answer)) {
                if(Integer.parseInt(answer) == 1) return true;
                else if(Integer.parseInt(answer) == 2) return false;
            } else {
                if (answer.equals("Да")) return true;
                else if(answer.equals("Нет")) return false;
            }
        }
        return false;
    }

    public void chooseSumToWithdraw(int sum) throws AtmException {
        int maxSum = getSumFromBankAccount();
        if(sum > maxSum) {
            System.out.println("На вашем счету лишь " + maxSum);
            if(isAllGood("Снять все деньги?")){
                sum = maxSum;
            } else {
                return;
            }
        }
        int newSum = dispenser.checkSum(sum);
        if(dispenser.isError()) {
            System.out.println(dispenser.getErrorDescription());
            if(isAllGood("Продолжить с новой суммой?")) {
                sum = newSum;
            } else {
                return;
            }
        }
        dispenser.prepareSum(sum); //Подготавливаем сумму
        //Создаем транзакцию

    }

    /**
     * Метод перевода суммы со счета клиента
     * на счет компании
     */
    public void payChecks() {
    }

    /**
     * Метод отправки команды на сервер банка
     * на внесение суммы на счет клиента
     */
    public int putBillsOnBankAccount(int maxSumInClientWallet, Map<String, String> availableBills) throws AtmException {
        int summ = billAcceptor.openBillAcceptor(maxSumInClientWallet, availableBills);
        System.out.println("Вы добавили денег на сумму " + billAcceptor.getBillAcceptorSum());
        makeBankTransaction("new_transaction_add_money", billAcceptor.getBillAcceptorSum());
        System.out.println("1.Внести на счет 2.Добавить сумму 3.Вернуть деньги");
        System.out.println("Типа саздаю транзакцию");
        return summ;
    }

    public void makeBankTransaction(String command, int added) throws AtmException {
        Map<String, String> toJson = new HashMap<>();
        toJson.put("command", command);
        toJson.put("atm_hash", getUniqHash());
        toJson.put("card_number", getCardAcceptor().getAccountNumber());
        toJson.put("sum", String.valueOf(added));
        sendTransaction(toJson);
    }

    public void comitTransaction(String command) throws AtmException {
        Map<String, String> toJson = new HashMap<>();
        toJson.put("command", command);
        toJson.put("atm_hash", getUniqHash());
        toJson.put("card_number", getCardAcceptor().getAccountNumber());
        toJson.put("sum", "");
        sendTransaction(toJson);
        if(isAllGood("Напечатать чек?")) {
            printer.print(command, "atm_hash", "card_number", "sum");
        }
    }

    public void rollbackTransaction(String command) throws AtmException {
        Map<String, String> toJson = new HashMap<>();
        toJson.put("command", command);
        toJson.put("atm_hash", getUniqHash());
        toJson.put("card_number", getCardAcceptor().getAccountNumber());
        toJson.put("sum", "");
        sendTransaction(toJson);
    }

    private void sendTransaction(Map<String,String>toJson) throws AtmException {
        String json = makeJson(toJson);
        Map<String,String> result = sendDataToTheBank(json);
        if(result.get("status").equals("true")) {
            setOperationStatus(true);
            System.out.println("ok");
            setOperationResult("1");
        } else {
            setOperationStatus(false);
            System.out.println("foo");
            setOperationResult("2");
        }
    }

    /**
     * Метод обнуляет купюроприемник
     */
    public void getMoneyBack() {
        billAcceptor.setBillAcceptorContains(new HashMap<>());
    }

    /**
     * Метод отправки команды выдать карту из картоприемника
     */
    public void eject() throws AtmException{
        setOperationCritical(false);
        cardAcceptor.cardEject();
    }

    /**
     * Простейшая проверка на целочесленное значение
     * @param string проверяемая строка
     * @return boolean
     */
    private boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

/**
 * Класс клиент связи с сервером банка
 */
class SocketClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Метод подключения к серверу
     * @param ip ип сервера
     * @param port порт сервера, должен соответсвовать порту который слушает сервер
     * @throws IOException ошибка ввода ввывода
     */
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    /**
     * Метод отправки сообщения на сервер
     * @param msg String строка которую отправляем на  сервер
     *            Для успешной обработки строка должна быть в формате json
     *            и иметь command=""
     * @return String ответ от сервера
     * @throws IOException ошибка ввода вывода
     */
    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    /**
     * Метод остановки подключения
     * если не закрыть будет сидеть и жрать память
     * @throws IOException ошибка ввода вывода
     */
    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}