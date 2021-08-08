import Atm.CardAcceptor;
import Card.Card;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
//import com.google.gson.Gson;

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
            if(line.contains("/")) newCard.setExpirationDate(line);
            if(line.contains(" ")) {
                String[] holder = line.split(" ");
                newCard.setAccountHolderName(holder[0]);
                newCard.setAccountHolderSoname(holder[1]);
            }
        }
        CardAcceptor cardAcceptor = new CardAcceptor(newCard);

        GreetClient client = new GreetClient();
        client.startConnection("127.0.0.1", 6666);
        String response = client.sendMessage("hello server");
        System.out.println(response);
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


