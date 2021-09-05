package Client;

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
    private static Client instance;
    private int sum;
    private Map<String, String> availableBills;

    private void setSum(int sum) {
        this.sum = sum;
    }

    public int getSum() { return sum; }

    public Map<String, String> getAvailableBills() { return availableBills; }

    private void setAvailableBills(Map<String, String> availableBills ) {
        this.availableBills = availableBills;
    }



    /**
     * Приватный конеструктор класса, как часть singleton
     */
    private Client(){
        Map<String,String> result = getBillFromWallet();
        setSumWallet(result);
        setAvailableBills(result);
    }

    /**
     * Реализация шаблона singleton, создаем instance только в том случае если его нет
     * @return instance
     */
    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    /**
     * Метод выбора карты из доступных в качестве карт используются файлы из папкм cards
     * @return
     */
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

    /**
     * Метод возвращает максимальную сумму которую сможет положить клиент на счет
     */
    public void checkWallet() {
        System.out.println("Максимальная сумма которую можно положить " + getSum() + "(" + getAvailableBills() + ")");
    }

    /**
     * Метод изменяет кол-во купюр по всем наминалам исходя из суммы которая пришла
     * @param money int сумма которую нужно изъять из кошелька
     */
    public boolean changeWalletContains(Map<String, String> money) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String moneyJson = gson.toJson(money);
        try {
            FileWriter myWriter = new FileWriter("src/main/java/Client/wallet");
            myWriter.write(moneyJson);
            myWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println("Кошелька нет, украли чтоли?");
            return false;
        }
    }

    /**
     * Вычитает купюры из кошелька по пришедшей сумме
     * @param sum int сумма которую нужно изъять из кошеля
     */
    public void calculateFromWallet(int sum) {
        List<String> availableBIlls = new LinkedList<>( getAvailableBills().keySet());
        availableBIlls.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        Map<String,String>money = getAvailableBills();
        for(String bill : availableBIlls) {
            int newBill = Integer.parseInt(bill);
            int amount = Integer.parseInt(money.get(bill));
            while (amount > 0) {
                if(sum >= newBill) {
                    amount--;
                    money.put(bill, String.valueOf(amount));
                    sum = sum - newBill;
                } else break;
            }
        }
        setAvailableBills(money);
        setSumWallet(money);
    }

    /**
     * Метод суммиррует пришедшую коллекцию с коллекецией сформированной из wallet
     * и переписыват wallet получившейся коллекцией
     * @param money коллекция
     */
    public void putBillsInTheWallet(Map<String, String> money) {
        Map<String,String> moneyInWallet = getAvailableBills();
        List<String> bills = new LinkedList<>(moneyInWallet.keySet());
        bills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        for(String bill : bills) {
            if(money.containsKey(bill)) {
                int amountIn = Integer.parseInt(money.get(bill));
                int amountWhole = Integer.parseInt(moneyInWallet.get(bill));
                moneyInWallet.put(bill, String.valueOf(amountIn+amountWhole));
            }
        }
        setAvailableBills(moneyInWallet);
        if(changeWalletContains(getAvailableBills())) {
            System.out.println("Деньги добавлены в кошелек");
        }
    }

    /**
     * Метод создает экземпляр класса Card и заполняет его поля
     * @param cardFile файл карты
     * @return экземпляр класса Card
     */
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
        setSum(sum);
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

}
