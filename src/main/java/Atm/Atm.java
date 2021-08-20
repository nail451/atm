package Atm;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionException;

import Card.Card;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Atm {

    private boolean operationCritical = false;
    private boolean operationStatus;
    private String operationResult;
    private int falseCounter = 0;
    private CardAcceptor cardAcceptor;
    private final BillAcceptor billAcceptor = new BillAcceptor();
    private Map<Integer, Integer> outputCassette;
    public final static int DELAYTIME = 10;

    /// get+set ///


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
    }

    public void newCard(Card newCard) {
        setCardAcceptor(new CardAcceptor(newCard));
    }

    /**
     * Метод отправки данных на сокет сервер банка
     * результат заносится в поля operationStatus и operationResult
     * @param data строка с данными
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

    public void checkPinStart(Scanner pin) throws Exception {
        checkPin(pin.nextLine());
    }

    public void checkPinAskPin(){
        System.out.println("Пожалуйста введите pin код");
    }

    public boolean errorHappened() throws AtmException{
        System.out.println("Произошла ошибка. Повторить дейстивие?");
        System.out.println("1.Да 2.Нет");
        Scanner nextMove = new Scanner(System.in);
        if (nextMove.hasNextLine()) {
            if (nextMove.nextInt() == 1 || nextMove.nextLine().equals("Да")) {
                return true;
            } else if (nextMove.nextInt() == 2 || nextMove.nextLine().equals("Нет")) {
                eject();
                return false;
            }
        }
        return false;
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

    /**
     * Метод отправки запроса на сервер банка с получением данных по соотвествующей карте
     */
    public void getDataFromBankAccount() throws AtmException {
        CardAcceptor cardAcceptor = getCardAcceptor();
        String[] keys = new String[2];
        keys[0] = "command";
        keys[1] = "card_number";
        String[] values = new String[2];
        values[0] = "get_data";
        values[1] = cardAcceptor.getAccountNumber() + "";
        String json = makeJson(keys, values);
        Map<String,String> result = sendDataToTheBank(json);
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

    public void giveOptions() {
        System.out.println("Выберите действие:");
        System.out.println("1.Оплата счета\n2.Внесение наличных на счет\n3.Снятие со счета\n0.Возврат карты");
    }

    /**
     * Метод отправки команды на диспенсер с выдачей суммы со счет клиента
     */
    public void getMoneyFromBankAccount() {
    }

    /**
     * Метод перевода суммы со счета клиента
     * на счет компании
     */
    public void payChecks() {
    }

    /**
     * Метод отправки команды в диспенсер
     * на ваыдачу суммы денег
     */
    public void billsDispensing() {
    }

    /**
     * Метод отправки команды на сервер банка
     * на внесение суммы на счет клиента
     */
    public int putBillsOnBankAccount(int maxSumInClientWallet, Map<String, String> availableBills) {
        int summ = billAcceptor.openBillAcceptor(maxSumInClientWallet, availableBills);
        System.out.println("Вы добавили денег на сумму " + billAcceptor.getBillAcceptorSum());
        System.out.println("1.Внести на счет 2.Добавить сумму 3.Вернуть деньги");
        System.out.println("Типа саздаю транзакцию");
        //TODO: createBankTransaction();
        return summ;
    }

    public void endTransaction() {
        System.out.println("Типа финалю транзакцию");
    }

    public void getMoneyBack() {
        System.out.println("Типа отдал бабосик");
    }

    /**
     * Метод проверкки даты истечения срока действия карты
     */
    public void checkCardExpiration(){
    }

    /**
     * Метод отправки команды выдать карту в картоприемник
     */
    public void eject() throws AtmException{
        setOperationCritical(false);
        cardAcceptor.cardEject();
    }
}

/**
 * Класс описывающий приемник купюр
 */
class BillAcceptor implements Receiving {

    private Map<Integer, Integer> billAcceptorContains = new HashMap<>();
    private Map<Integer, Integer> inputCassette;
    private TimerTask task;

    public BillAcceptor(){
        inputCassette = checkInputCassette();
    }

    public Map<Integer, Integer> getBillAcceptorContains() {
        return billAcceptorContains;
    }

    /**
     * Геттер суммы по контейнеру валюты
     * @return сумма купюр
     */
    public int getBillAcceptorSum() {
        Map<Integer, Integer> container = getBillAcceptorContains();
        Object[] bills = container.keySet().toArray();
        int sum = 0;
        for (Object i : bills) {
            sum += Integer.valueOf((Integer) i) * container.get(Integer.valueOf((Integer) i));
        }
        return sum;
    }

    /**
     * Сеттер временного хранилица валюты
     * Суммирует кол-во купюр из текущего и нового
     * Если пришел пустой контейнер обнуляет текущий
     * @param billAcceptorContains колекция в формате <Номинал = Кол-во>
     */
    public void setBillAcceptorContains(Map<Integer, Integer> billAcceptorContains) {
        Map<Integer, Integer> billsInAcceptor = getBillAcceptorContains();
        if(billsInAcceptor.isEmpty()) {
            this.billAcceptorContains = billAcceptorContains;
        } else {
            if (!billAcceptorContains.isEmpty()) {
                List<Integer> bills = new LinkedList<>(billAcceptorContains.keySet());
                for (Integer i : bills) {
                    int nowAdd = billAcceptorContains.get(i);
                    if(billsInAcceptor.containsKey(i)) {
                        int nowIn = billsInAcceptor.get(i);
                        billsInAcceptor.put(i, nowIn + nowAdd);
                    } else {
                        billsInAcceptor.put(i, nowAdd);
                    }
                }
                this.billAcceptorContains = billsInAcceptor;
            } else {
                this.billAcceptorContains = billAcceptorContains;
            }
        }
    }

    public Map<Integer, Integer> getInputCassette() {
        return inputCassette;
    }

    public void setInputCassette(Map<Integer, Integer> inputCassette) {
        this.inputCassette = inputCassette;
    }

    private Map<Integer, Integer> checkInputCassette() {
        File atmSafe = new File("src/main/java/Atm/atm_safe");
        Scanner atmSafeData = null;
        try {
            atmSafeData = new Scanner(atmSafe);
        } catch (FileNotFoundException e) {
            System.out.println("Сейф вскрыт, кассеты отсутвуют");
        }
        String data = "";
        assert atmSafeData != null;
        if (atmSafeData.hasNextLine()) {
            data = atmSafeData.nextLine();
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Map<String, Map<Integer, Integer>> result = gson.fromJson(data, HashMap.class);
        return result.get("input_cassette");

    }

    /**
     * Метод открытия агрегата
     * @return
     */
    public int openBillAcceptor(int maxUCan, Map<String, String> availableBills) {
        System.out.println( "Пожалуйста положите купюру или пачку в купюроприемник" );
        String error = "Пожалуйста укажите сумму которую вы хотелибы внести на счет\n";
        Map<Integer, Integer> money = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < Atm.DELAYTIME * 1000
                && !in.ready()) {
            }
            if (in.ready()) {
                String newLine = in.readLine();
                if(Integer.parseInt(newLine) <= maxUCan) {
                    try {
                        money = getBillsFromWallet(Integer.parseInt(newLine), availableBills);
                        setBillAcceptorContains(money);
                    } catch (Error e) {
                        System.out.println("Не хватает соответсвующих купюр для внесегия нужной суммы");
                    }
                } else {
                    System.out.println("Нехватает наличных для внесения полной суммы");
                    System.out.println("Будет внесено " + maxUCan);
                    money = getBillsFromWallet(maxUCan, availableBills);
                    setBillAcceptorContains(money);
                }
            } else {
                System.out.println("В купрюроприемнике отсутсвуют купюры");
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(error);
        }
        return sumMoneyOnIteration(money);
    }

    /**
     * Метод считает сумму из коллекции формата <номинал=кол-во>
     * @param money карта
     * @return int сумма
     */
    public int sumMoneyOnIteration(Map<Integer, Integer> money) {
        List<Integer> bills = new ArrayList<>(money.keySet());
        int sum = 0;
        for (Integer i : bills) {
            sum += i * money.get(i);
        }
        return sum;
    }

    /**
     * Метод выбирает купюры из массива доступных пока не наберется указанная сумма
     * @param neededMoney int указанная сумма
     * @param availableMoney map доступные купюры
     * @return map коллекция выбранных купюр
     */
    public Map<Integer, Integer> getBillsFromWallet(int neededMoney, Map<String, String>availableMoney) {
        List<String> allBills =  new LinkedList<>(availableMoney.keySet());
        allBills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        Map<Integer, Integer> money = new HashMap<>();
        for(String i : allBills) {
            int bill = Integer.parseInt(i);
            if(neededMoney >= bill) {
                int needBillAmount = neededMoney/bill;
                int billAmountAvailable = Integer.parseInt(availableMoney.get(i));
                if(billAmountAvailable >= needBillAmount) {
                    neededMoney = neededMoney%bill;
                    money.put(bill, needBillAmount);
                    if(neededMoney == 0) break;
                } else if(billAmountAvailable > 0) {
                    neededMoney = neededMoney - (bill*billAmountAvailable);
                    money.put(bill, billAmountAvailable);
                }
            }
        }
        if(neededMoney != 0) throw new Error();
        return money;
    }

    /**
     * Метод закрытия агрегата
     * Запускает метод receive
     */
    public void closeBillAcceptor(){
    }

    /**
     * Реализация метода интерфейса receiving
     * Проверет наличие денег
     * пересичитывает их и определяет наминал
     */
    @Override
    public void receive() {
    }
}

/**
 * Класс описывающий картоприемник
 */
class CardAcceptor implements Receiving {
    private Card insertedCard;
    private boolean cardInside;

    private String accountNumber;
    private String expirationDate;
    private String holder;

    /**
     * Конструктор класса,
     * Получает карту и данные с нее
     * @param newCard Card
     */
    public CardAcceptor(Card newCard) {
        insertedCard = newCard;
        receive();
        cardInside = isValidCard(newCard);
    }

    /// get+set ///

    public boolean getCardInside() {
        return cardInside;
    }

    private void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber(){
        return accountNumber;
    }

    /**
     * Сеттер поля expirationDate
     * Переводит дату хранящуюся на карте в unix_timestamp
     * @param expirationDate Строка с датой
     */
    private void setExpirationDate(String expirationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        Date parsedDate;
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

    /// get+set ///

    /**
     * Реализация метода из интрефейса Receiving
     * Получет данные с карты и раскладывает их по
     * соотвествующим полям
     */
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

    /**
     * Вывод карты
     */
    public void cardEject() throws AtmException {
        insertedCard = null;
        cardInside = false;
        throw new AtmException("Заберите карту\n\n", true);
    }

    /**
     * Проверка карты на валидность
     * проверяет по маске значиеня на карте
     * @param newCard объект класса Card
     * @return boolean
     */
    private boolean isValidCard(Card newCard){
        boolean isValid = false;
        if(Objects.nonNull(accountNumber) && isNumeric(accountNumber) && accountNumber.length() == 16) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Проверяет строку на полное содержание цифр
     * @param str строка на проверку
     * @return boolean результат проверки
     */
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

/**
 * Класс отвечаеющий за расчет и выдачу денег
 */
class Dispenser implements Issuing {
    private boolean billsInTheDispenser;
    private short billsInTheDispenserCount;
    private HashMap<Integer, Integer> arrayOfBillsInTheDispenser = new HashMap<>();
    /**
     * Метод проверящий доступное количество купюр всех достпупных наминалов
     */
    public void checkAvailableBills() {
    }
}

/**
 * Класс отвечающий за формирование и выдачу чеков
 */
class Printer implements Issuing {
    private int receiptPaper;
    private boolean receiptPaperError;

    public void createNewReceipt(){
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