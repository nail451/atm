package Atm;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import Bank.Commands.*;
import Card.Card;
import com.google.common.hash.Hashing;
import com.google.gson.*;

/**
 * Основной класс описывающий работу банкомата
 */
public class Atm implements InteractiveAtm {

    private static Atm instance;
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
    private String getUniqHash() { return uniqHash; }

    private void setUniqHash(String uniqHash) { this.uniqHash = uniqHash; }

    public boolean isOperationCritical() { return operationCritical; }

    public boolean getOperationStatus() { return operationStatus; }

    public String getOperationResult() { return operationResult; }

    public CardAcceptor getCardAcceptor() { return cardAcceptor; }

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

    public BillAcceptor getBillAcceptor() { return billAcceptor; }

    public Dispenser getDispenser() { return dispenser; }

    public Printer getPrinter() { return printer; }
    /// get+set ///

    /**
     * Приватный конеструктор класса, как часть singleton
     */
    private Atm(){
        System.out.println("Пожалуйста вставьте карту в картоприемник");
        setUniqHash(generateRandomHash());
    }

    /**
     * Реализация шаблона singleton, создаем instance только в том случае если его нет
     * @return instance
     */
    public static Atm getInstance() {
        if (instance == null) {
            instance = new Atm();
        }
        return instance;
    }

    /**
     * Метод объявляет новый экземпляр класса CardAcceptor и передает в него объект класса Card
     * @param newCard объект класса Card
     */
    public void getCard(Card newCard) {
        setCardAcceptor(new CardAcceptor(newCard));
    }

    /**
     * Метод прооверки соотвествия введеного пинкода сохраненному в банке
     * Собирает hashmap коллекцию
     * Преобразует коллекцию в json
     * Отправлет json на сервер
     * Устанавливает результаты в operationStatus и operationResult
     * @param enteredPinCode строка с введенным пин кодом
     */
    public void checkPin(String enteredPinCode) throws Exception {
        if(getCardAcceptor().getCardInside()) {
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

                Map<String, String> toClassFields = new HashMap<>();
                toClassFields.put("pinCode", hashedPinCode + "");
                toClassFields.put("expirationDate", getCardAcceptor().getExpirationDate());
                toClassFields.put("holderName", getCardAcceptor().getHolder());
                toClassFields.put("accountNumber", getCardAcceptor().getAccountNumber());
                Map<String, Map<String,String>> fields = makeFieldsMap("CheckPin", toClassFields);

                Transactions checkPin = new Transactions();
                checkPin.setFields(fields);
                Map<String,String> result = checkPin.makeTransaction();

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
     * Метод вызывает метод запроса данных с сервера и
     * обрабатывает пришедший результат
     * результат устанавливается в operationStatus и operationResult
     * @throws AtmException ошибка atm
     */
    public void getDataFromBankAccount() throws AtmException {
        Map<String,String> result = getDataFromBank();
        if(result.get("status").equals("ok")) {
            setOperationStatus(true);
            setOperationResult("Здравсвуйте " + result.get("name") + "\nВаш баланс счета: " + result.get("balance"));
        } else {
            if(result.get("status").equals("card_on_hold")) {
                setOperationStatus(false);
                setOperationResult("Карта заблокирована банком\n\n");
            } else if (result.get("status").equals("account_on_hold")) {
                setOperationStatus(false);
                setOperationResult("Счет привязанный к карте заблокирован\n\n");
            } else if (result.get("status").equals("expired")) {
                setOperationStatus(false);
                setOperationResult("Срок действия карты истек\n\n");
            }
        }
    }

    /**
     * Метод формирует строку в которой описывается кол-во доступных купур в банкомате
     */
    public void checkOutputCassette() {
        Map<String,String> billsAvailable = getDispenser().checkCassette();
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
     * @throws AtmException ошибка банкомата
     */
    public int getSumFromBankAccount() throws AtmException {
        Map<String,String> result = getDataFromBank();
        return Integer.parseInt(result.get("balance"));
    }

    /**
     * Метод обрабатывает введенную клиентом сумму которую он хотел бы снять
     * Обрабатывает ошибки недостачи средсв на счету,
     * небостачи купюр нужного номинала
     * Собирает коллекцию с коммандой и отправляет ее на сервер
     * @param sum строка
     * @throws AtmException критическая ошибка банкомата
     * @throws Error не критичная ошибка
     */
    public void chooseSumToWithdraw(int sum) throws AtmException, Error {
        int maxSum = getSumFromBankAccount();
        if(sum > maxSum) {
            System.out.println("На вашем счету лишь " + maxSum);
            if(isAllGood("Снять все деньги?")){
                sum = maxSum;
            } else {
                throw new Error();
            }
        }
        int newSum = getDispenser().checkSum(sum);
        if(getDispenser().isError() && newSum != 0) {
            System.out.println(getDispenser().getErrorDescription());
            if(isAllGood("Продолжить с новой суммой?")) {
                sum = newSum;
            } else {
                throw new Error();
            }
        } else if (getDispenser().isError() && newSum == 0) {
            System.out.println(getDispenser().getErrorDescription());
            throw new Error();
        }
        getDispenser().prepareSum(sum); //Подготавливаем сумму

        Map<String, String> toClassFields = new HashMap<>();
        toClassFields.put("atmHash", getUniqHash());
        toClassFields.put("accountNumber", getCardAcceptor().getAccountNumber());
        toClassFields.put("sum", String.valueOf(sum));
        Map<String, Map<String,String>> fields = makeFieldsMap("PullMoney", toClassFields);

        Transactions pullBills = new Transactions();
        pullBills.setFields(fields);
        pullBills.makeTransaction();
    }

    /**
     * Метод возвращет содержимое диспенсера и очищает его
     * @return коллекция
     */
    public Map<String,String> giveMoney() {
        Map<String,String> money = getDispenser().getBillsInTheDispenser();
        getDispenser().setBillsInTheDispenser(new HashMap<>());
        return money;
    }

    /**
     * Метод перевода суммы со счета клиента
     * на счет компании
     */
    public void payChecks() {
    }

    /**
     * Метод обращается вложенную клиентом в купюроприемник сумму денег
     * формирует команду на старт транзакции и предлогает дальнейшие действия
     * @param maxSumInClientWallet костыль - банкомат знает сколько максимум есть денег у клиента
     * @param availableBills костыль - банкомат знает какие купюры формируют сумму
     * @return int сумма денег в купюроприемнике
     * @throws AtmException ошибка
     */
    public int putBillsOnBankAccount(int maxSumInClientWallet, Map<String, String> availableBills) throws AtmException {
        int summ = getBillAcceptor().openBillAcceptor(maxSumInClientWallet, availableBills);
        if(getBillAcceptor().getBillAcceptorSum() != 0) {
            System.out.println("Вы добавили денег на сумму " + getBillAcceptor().getBillAcceptorSum());

            Map<String, String> toClassFields = new HashMap<>();
            toClassFields.put("atmHash", getUniqHash());
            toClassFields.put("accountNumber", getCardAcceptor().getAccountNumber());
            toClassFields.put("sum", String.valueOf(getBillAcceptor().getBillAcceptorSum()));
            Map<String, Map<String,String>> fields = makeFieldsMap("PutMoney", toClassFields);

            Transactions putBills = new Transactions();
            putBills.setFields(fields);
            putBills.makeTransaction();

            System.out.println("1.Внести на счет 2.Добавить сумму 3.Вернуть деньги");
        }
        return summ;
    }

    /**
     * Запрос подтвержения у клиента.
     * @return boolean
     */
    public boolean askCommit() {
        return isAllGood("Снять указанную сумму?");
    }

    /**
     * Финализация транзации коммитом
     * @throws AtmException ошибка банкомата
     */
    public void commit() throws AtmException {
        Map<String,String> toClassFields = new HashMap<>();
        toClassFields.put("atmHash", getUniqHash());

        Map<String, Map<String,String>> fields = makeFieldsMap("Commit", toClassFields);

        Transactions commit = new Transactions();
        commit.setFields(fields);
        commit.makeTransaction();
    }

    /**
     * Финализация транзации откатом
     * @throws AtmException ошибка банкомата
     */
    public void rollback() throws AtmException {
        Map<String,String> toClassFields = new HashMap<>();
        toClassFields.put("atmHash", getUniqHash());

        Map<String, Map<String,String>> fields = makeFieldsMap("Rollback", toClassFields);

        Transactions rollback = new Transactions();
        rollback.setFields(fields);
        rollback.makeTransaction();
    }

    /**
     * Метод обнуляет купюроприемник
     */
    public void getMoneyBack() {
        getBillAcceptor().setBillAcceptorContains(new HashMap<>());
    }

    /**
     * Метод отправки команды выдать карту из картоприемника
     */
    public void eject() throws AtmException{
        setOperationCritical(false);
        getCardAcceptor().cardEject();
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
     * Метод генерации 7 символьной строки
     * для унификации запущенного экземпляра класса
     */
    private String generateRandomHash() {
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
     * Вспомогательный метод для сбора коллекции
     * @param command String команда серверу
     * @param toClassFields Map коллекция полей необходимая для выполнения команды
     * @return Map<String, Map<String, String>>
     */
    private Map<String, Map<String,String>> makeFieldsMap(String command, Map<String,String> toClassFields) {
        Map<String, String> commandField = new HashMap<>();
        commandField.put("command", command);
        Map<String, Map<String,String>> fields = new HashMap<>();
        fields.put("command", commandField);
        fields.put("toClass", toClassFields);
        return fields;
    }

    /**
     * Метод запрашивающий подтверждение у клиента
     * @param yesNoAskString строка содержащая вопрос
     * @return boolean
     */
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

    /**
     * Метод собирает коллекцию с коммандой GetData,
     * формирует из нее строку и отправляет ее на сервер
     * @return Map<String,String> результат выполнения команды
     * @throws AtmException при ошибке коннекта тормозим выполнение программы
     */
    private Map<String,String> getDataFromBank() throws AtmException {
        Map<String, String> toClassFields = new HashMap<>();
        toClassFields.put("atmHash", getUniqHash());
        toClassFields.put("accountNumber", getCardAcceptor().getAccountNumber());
        Map<String, Map<String,String>> fields = makeFieldsMap("GetData", toClassFields);

        Transactions getData = new Transactions();
        getData.setFields(fields);
        return getData.makeTransaction();
    }
}

/**
 * Класс клиент связи с сервером банка
 */
class SocketClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /// get+set ///
    private Socket getClientSocket() { return clientSocket; }

    private void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }

    public PrintWriter getOut() { return out; }

    public void setOut(PrintWriter out) { this.out = out; }

    public BufferedReader getIn() { return in; }

    public void setIn(BufferedReader in) { this.in = in; }
    /// get+set ///

    /**
     * Метод подключения к серверу
     * @param ip ип сервера
     * @param port порт сервера, должен соответсвовать порту который слушает сервер
     * @throws IOException ошибка ввода ввывода
     */
    public void startConnection(String ip, int port) throws IOException {
        setClientSocket(new Socket(ip, port));
        setOut(new PrintWriter(getClientSocket().getOutputStream(), true));
        setIn(new BufferedReader(new InputStreamReader(getClientSocket().getInputStream())));
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
        getOut().println(msg);
        return getIn().readLine();
    }

    /**
     * Метод остановки подключения
     * если не закрыть будет сидеть и жрать память
     * @throws IOException ошибка ввода вывода
     */
    public void stopConnection() throws IOException {
        getIn().close();
        getOut().close();
        getClientSocket().close();
    }
}

/**
 * Класс отвечающий за работу с сервером
 * для успешной работы класса необходимо
 * установить все поля за исключением otherFields (опцианально)
 * и вызвать один из методов создающих транзакцию
 */
class Transactions {

    /**
     * Поле содержащее в себе строку указывающую какое действие
     * необходимо исполнить серверу
     * Досьупные комманды:
     * check_pin проверка пинкода
     * get_data запрос данных о пользователе
     * transaction_add_money создание транзации на добавление денег на счет
     * rollback откат транзакции
     * commit комит транзакции
     */
    private Map<String,Map<String,String>> fields;

    public void setFields(Map<String,Map<String,String>> fields) { this.fields = fields; }

    private Map<String,Map<String,String>> getFields() { return fields; }


    /**
     * Метод формирует из map - json и отправляет на сервер
     * авто коммита при этом не происходит, как следствие
     * необходимо транзакцию нужно либо закоммитить либо роллбэкнуть
     * @throws AtmException ошибка банкомата
     */
    public Map<String, String> makeTransaction() throws AtmException {
        String json = makeJson();
        return sendDataToTheBank(json);
    }



    /**
     * Метод формирующий json строку из hashmap коллекции
     * @return String формата json
     */
    private String makeJson() {
        Map<String,Map<String,String>> toJson = getFields();
        Gson gson = new GsonBuilder()
                .create();
        return gson.toJson(toJson);
    }

    /**
     * Метод отправки данных на сокет сервер банка
     * сразу перегоняет ответ в map
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
}
