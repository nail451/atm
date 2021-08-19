package Client;

import Atm.Atm;
import Card.Card;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Класс клиент
 */
public class Client {

    String wallet;

    public Card selectCard() {
        while (true) {
            System.out.println("//Введите номер карты из предложенного списка//");
            File cardsDirectory = new File("./cards/");
            short cardsCounter = 0;
            for (File cardsFiles : Objects.requireNonNull(cardsDirectory.listFiles())) {
                cardsCounter++;
                System.out.println(cardsCounter + ". " + cardsFiles.getName());
            }
            Scanner chooseCard = new Scanner(System.in);
            int cardNumber = chooseCard.nextInt() - 1;
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

    public void putCardInCardAcceptor(Atm atm,Card card) {
        atm.newCard(card);
    }

    public Scanner fillPinField() {
        return new Scanner(System.in);
    }

    public Scanner chooseOption() {
        return new Scanner(System.in);
    }

    public void checkWallet() {
        File wallet = new File("wallet");
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
        HashMap<String, Integer> result = gson.fromJson(data, HashMap.class);
        System.out.println(result);
    }

    public void putMoney() {

    }

    public void getMoney() {

    }
}
