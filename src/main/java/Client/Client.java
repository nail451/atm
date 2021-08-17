package Client;

import Atm.Atm;
import Card.Card;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Класс клиент
 */
public class Client {

    public Card selectCard() {
        System.out.println("//Введите номер карты из предложенного списка//");
        File cardsDirectory = new File("./cards/");
        short cardsCounter = 0;
        for( File cardsFiles : Objects.requireNonNull(cardsDirectory.listFiles())){
            cardsCounter++;
            System.out.println( cardsCounter + ". " + cardsFiles.getName() );
        }
        Scanner chooseCard = new Scanner(System.in);
        int cardNumber = chooseCard.nextInt() - 1;
        File cardFile =  Objects.requireNonNull(cardsDirectory.listFiles())[cardNumber];
        return createCardFromFile(cardFile);
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

    /**
     * Метод для снятия денег со счета в банке
     * клиент может снять часть или все деньги со счета
     */
    public void getMoneyFromBankAccount() {

    }

    /**
     * Метод для добавления денег на счет в банке
     * клиент может внести деньги на свой счет в банке
     */
    public void putMoneyToBankAccount() {

    }

    /**
     * Метод для оплаты счетов выставленных по клиентскому счету
     */
    public void fillChecks() {

    }
}
