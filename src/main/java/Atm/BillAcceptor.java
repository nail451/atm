package Atm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;

/**
 * Класс описывающий приемник купюр
 */
public class BillAcceptor {

    private Map<Integer, Integer> billAcceptorContains = new HashMap<>();
    private final static int DELAYTIME = 10;
    private Map<Integer, Integer> inputCassette;

    public BillAcceptor(){
        setInputCassette(checkInputCassette());
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

    public void setInputCassette(Map<Integer, Integer> inputCassette) {
        this.inputCassette = inputCassette;
    }

    /**
     * Метод открытия аггрегата для принятия денег
     * Открывает аггрегат на DELAYTIME секунд для внесения денежной суммы
     * устанавлиает поле billAcceptorContains
     * @param maxUCan максимально доступная клиенту сумма
     * @param availableBills доступные клиенту купюры и их кол-во
     * @return сумма вложенная клиентом
     */
    public int openBillAcceptor(int maxUCan, Map<String, String> availableBills) {
        System.out.println( "Пожалуйста положите купюру или пачку в купюроприемник" );
        String error = "Пожалуйста укажите сумму которую вы хотелибы внести на счет\n";
        Map<Integer, Integer> money = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            long startTime = System.currentTimeMillis();
            System.out.println( "Купюроприемник будет закрыт через " + DELAYTIME + " секунд");
            while ((System.currentTimeMillis() - startTime) < DELAYTIME * 1000 && !in.ready()) {
            }
            if (in.ready()) {
                String newLine = in.readLine();
                if(Integer.parseInt(newLine) <= maxUCan) {
                    try {
                        money = getBillsFromWallet(Integer.parseInt(newLine), availableBills);
                        setBillAcceptorContains(money);
                    } catch (Error e) {
                        System.out.println("Не хватает соответсвующих купюр для внесения нужной суммы");
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
     * Проверяет входную кассету с деньгами
     * Переводит данные из json в hashmap
     * возвращяет состояние в виде hashmap
     * @return hashmap в формате <Номинал=количество>
     */
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
}
