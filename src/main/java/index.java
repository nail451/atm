import Atm.*;
import Card.Card;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;



public class index {
    public static void main(String[] args) throws IOException {
        System.out.println("Пожалуйста вставьте карту в картоприемник");
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
        Card newCard = createCardFromFile(cardFile);

        CardAcceptor cardAcceptor = new CardAcceptor(newCard);
        Scanner pin = new Scanner(System.in);
        Atm atm = new Atm();
        System.out.println("Пожалуйста введите pin код");
        atm.checkPin(pin.nextLine());



        /**
         * TODO:
         * Введите пин код
         * обработка пин кода принято/не принято
         * Выберените действие
         * Оплата счето/внесение на счет/снятие со счета
         */
    }

    private static Card createCardFromFile(File cardFile) {
        Scanner cardFileData = null;
        Card newCard = new Card();
        try {
            cardFileData = new Scanner(cardFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            assert cardFileData != null;
            if (!cardFileData.hasNextLine()) break;
            String line = cardFileData.nextLine();
            if (line.length() == 12 && isNumeric(line)) newCard.setAccountNumber(line);
            if (line.contains("/")) newCard.setExpirationDate(line);
            if (line.contains(" ")) {
                String[] holder = line.split(" ");
                newCard.setAccountHolderName(holder[0]);
                newCard.setAccountHolderSoname(holder[1]);
            }
        }
        return  newCard;
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
