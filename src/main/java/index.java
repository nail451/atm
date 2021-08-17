import Atm.*;
import Card.Card;
import Client.Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;



public class index {
    public static void main(String[] args) throws IOException {

        Client client = new Client();
        Atm atm = new Atm();

        Card card = client.selectCard();
        client.putCardInCardAcceptor(atm, card);

        int counter = 0;
        while (true) {
            counter++;
            if(counter>= 6) {
                System.out.println("Слишком много попыток входа, карта будет временно заблокирована");
            }
            System.out.println("Пожалуйста введите pin код");
            Scanner pin = client.fillPinField();
            String resp = atm.checkPin(pin.nextLine());
            if(resp.equals("true")) break;
            else {
                System.out.println("Произошла ошибка. Повторить дейстивие?");
                System.out.println("1.Да 2.Нет");
                Scanner nextMove = new Scanner(System.in);
                if(nextMove.hasNextLine()) {
                    if(nextMove.nextInt() == 1 || nextMove.nextLine().equals("Да")) {
                        continue;
                    } else if(nextMove.nextInt() == 2 || nextMove.nextLine().equals("Нет")) {
                        atm.eject();
                        String[] voidArray = new String[0];
                        main(voidArray);
                        break;
                    }
                }
            }
        }


        System.out.println("Выберите действие:");
        System.out.println("1.Оплата счета\n2.Внесение наличных на счет\n3.Снятие со счета\n0.Возврат карты");
        Scanner nextMove = new Scanner(System.in);
        switch (nextMove.nextInt()) {
            case 1:
                client.fillChecks();
                break;
            case 2:
                client.getMoneyFromBankAccount();
                break;
            case 3:
                client.putMoneyToBankAccount();
                break;
            case 0:
                atm.eject();
                String[] voidArray = new String[0];
                main(voidArray);
                break;
            default:
                break;
        }

        /**
         * TODO:
         * обработка пин кода принято/не принято
         * Выберените действие
         * Оплата счето/внесение на счет/снятие со счета
         */
    }


}
