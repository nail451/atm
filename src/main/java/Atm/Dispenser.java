package Atm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Класс отвечаеющий за расчет и выдачу денег
 */
class Dispenser {

    private Map<String,String> billsInTheDispenser;
    private Map<String,String> cassette;
    private String errorDescription;
    private boolean error;

    /**
     * При первой инициализации заполняем поле cassette
     */
    public Dispenser() {
        setCassette(checkCassette());
    }

    private Map<String, String> getCassette() { return cassette; }

    private void setCassette(Map<String, String> cassette) { this.cassette = cassette; }

    public String getErrorDescription() { return errorDescription; }

    private void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }

    public boolean isError() { return error; }

    private void setError(boolean error) { this.error = error; }

    public Map<String, String> getBillsInTheDispenser() { return billsInTheDispenser; }

    public void setBillsInTheDispenser(Map<String, String> billsInTheDispenser) { this.billsInTheDispenser = billsInTheDispenser; }

    /**
     * Метод проверящий доступное количество купюр всех достпупных наминалов
     * @return hashmap <Номинал-количество>
     */
    public Map<String, String> checkCassette() {
        File cassette = new File("src/main/java/Atm/atm_safe");
        Scanner cassetteData = null;
        try {
            cassetteData = new Scanner(cassette);
        } catch (FileNotFoundException e) {
            System.out.println("Отсуствует кассетта для выдачи");
        }
        String data = "";
        assert cassetteData != null;
        if (cassetteData.hasNextLine()) {
            data = cassetteData.nextLine();
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Map<String, Map<String, String>> result = gson.fromJson(data, HashMap.class);
        return  result.get("output_cassette");
    }

    /**
     * Проверка доступности суммы для снятия
     * @param sum
     * @return
     */
    public int checkSum(int sum) {
        setError(false);
        setErrorDescription("");
        Map<String,String> cassette = getCassette();
        List<String> availableBills = new LinkedList<>(cassette.keySet());
        availableBills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        int sumLeft = sum; 
        int billCounter = 0;
        for(String i : availableBills) {
            int bill = Integer.parseInt(i);
            int amountNeeded = sumLeft/bill;
            if(amountNeeded > 0 && Integer.parseInt(cassette.get(i)) >= amountNeeded) {
                billCounter += sumLeft/bill;
                sumLeft = sumLeft - (sumLeft/bill * bill);
            }            
            if(billCounter == 40 && sumLeft != 0) {
                setError(true);
                setErrorDescription("Превышено максимальное кол-во купюр.\nМаксимальная сумма которую сможет выдать банкомат: " + (sum - sumLeft));
                break;
            }
        }
        if(sumLeft > 0) {
            if(sum - sumLeft == 0) {
                setError(true);
                setErrorDescription("Банкомат не может выдать указанную сумму");
            } else {
                setError(true);
                setErrorDescription("Максимальная сумма которую сможет выдать банкомат: " + (sum - sumLeft));
            }
        }
        return sum-sumLeft;
    }

    public void prepareSum(int sum) {

        Map<String,String> cassette = getCassette();
        Map<String, String> billsInDispenser = new HashMap<>();
        List<String> availableBills = new LinkedList<>(cassette.keySet());
        availableBills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        int sumLeft = sum;
        for(String i : availableBills) {
            int bill = Integer.parseInt(i);
            int amountNeeded = sumLeft/bill;
            if(amountNeeded > 0 && Integer.parseInt(cassette.get(i)) >= amountNeeded) {
                billsInDispenser.put(i, String.valueOf(sumLeft/bill));
                sumLeft = sumLeft - (sumLeft/bill * bill);
            }
        }
        setBillsInTheDispenser(billsInDispenser);
    }
}
