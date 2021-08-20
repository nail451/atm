package Client;

import Atm.Atm;
import Card.Card;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Класс клиент
 */
public class Client {

    int sum;
    Map<String, String> availableBills;

    public int getSum() { return sum; }

    public Map<String, String> getAvailableBills() { return availableBills; }

    public Card selectCard() {
        while (true) {
            System.out.println("//Введите номер карты из предложенного списка//");
            File cardsDirectory = new File("./cards/");
            short cardsCounter = 0;
            for (File cardsFiles : Objects.requireNonNull(cardsDirectory.listFiles())) {
                cardsCounter++;
                System.out.println(cardsCounter + ". " + cardsFiles.getName());
            }
            System.out.println("0. Выход");
            int chooseCard = new Scanner(System.in).nextInt();
            if(chooseCard == 0) System.exit(0);
            int cardNumber = chooseCard - 1;
            try {
                File cardFile = Objects.requireNonNull(cardsDirectory.listFiles())[cardNumber];
                return createCardFromFile(cardFile);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("//Ошибка ввода//");
            }
        }
    }

    private Card createCardFromFile(File cardFile) {
        Scanner cardFileData = null;
        Card newCard = new Card();
        try {
            cardFileData = new Scanner(cardFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> data = new ArrayList<>();
        while (true) {
            assert cardFileData != null;
            if (!cardFileData.hasNextLine()) break;
            data.add(cardFileData.nextLine());
        }
        newCard.setCardData(data);
        return  newCard;
    }


    public void putCardInCardAcceptor(Atm atm,Card card) { atm.newCard(card); }

    public Scanner enterAnswer() { return new Scanner(System.in); }

    /**
     * Метод возвращает максимальную сумму которую сможет положить клиент на счет
     * @return int сумма
     */
    public void checkWallet() {
        Map<String,String> result = getBillFromWallet();
        setSumWallet(result);
        setAvailableBills();
        System.out.println("Максимальная сумма которую можно положить " + getSum() + "(" + getAvailableBills() + ")");
    }

    /**
     * Метод возвращает доступное кол-во денег в виде колекции формата <Номинал=Кол-во>
     * @return <Номинал=Кол-во>
     */
    private Map<String, String> getBillFromWallet(){
        File wallet = new File("src/main/java/Client/wallet");
        Scanner walletData = null;
        try {
            walletData = new Scanner(wallet);
        } catch (FileNotFoundException e) {
            System.out.println("Кошелька нет, украли чтоли?");
        }
        String data = "";
        assert walletData != null;
        if (walletData.hasNextLine()) {
            data = walletData.nextLine();
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Map<String, String> result = gson.fromJson(data, HashMap.class);
        return  result;
    }

    /**
     * Метод изменяет кол-во купюр по всем наминалам исходя из суммы которая пришла
     * @param money int сумма которую нужно изъять из кошелька
     */
    private void changeWalletContains(Map<String, String> money) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String moneyJson = gson.toJson(money);
        try {
            FileWriter myWriter = new FileWriter("src/main/java/Client/wallet");
            myWriter.write(moneyJson);
            myWriter.close();
            System.out.println("Деньги изьяты из кошелька");
        } catch (IOException e) {
            System.out.println("Кошелька нет, украли чтоли?");
        }
    }

    /**
     * Пересчитывает все пришедшие купюры и возваращает их сумму
     * @param wallet <номинал=количество>
     */
    private void setSumWallet(Map<String, String> wallet) {
        List<String> bills = new LinkedList<>(wallet.keySet());
        bills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        int sum = 0;
        for (String i : bills) {
            sum +=  Integer.parseInt(i) * Integer.parseInt(wallet.get(i));
        }
        this.sum = sum;
    }

    /**
     * Метод устанавливает поле картой с доступными купюрами
     */
    private void setAvailableBills() {
        this.availableBills = getBillFromWallet();
    }

    /**
     * Вычитает купюры из кошелька по пришедшей сумме
     * @param sum int сумма которую нужно изъять из кошеля
     */
    public void calculateFromWallet(int sum) {
        List<String> availableBIlls = new LinkedList<>( getBillFromWallet().keySet());
        availableBIlls.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        Map<String,String>money = getBillFromWallet();
        for(String bill : availableBIlls) {
            int newBill = Integer.parseInt(bill);
            int amount = Integer.parseInt(money.get(bill)) - sum/newBill;
            if(sum/newBill > 0 && amount >= 0) {
                money.put(bill, String.valueOf(amount));
                sum = sum-(newBill*(sum/newBill));
            }
        }
        changeWalletContains(money);
    }

    public void putMoney() {

    }
}
