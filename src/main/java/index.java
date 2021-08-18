import Atm.*;
import Atm.AtmException;
import Card.Card;
import Client.Client;

import java.io.IOException;
import java.util.Scanner;



public class index {
    public static void main(String[] args) throws IOException {

        Client client = new Client();
        Atm atm = new Atm();
        while (true) {
            try {
                System.out.println(1);
                Card card = client.selectCard();
                client.putCardInCardAcceptor(atm, card);
                atm.checkPinAskPin();
                Scanner pin = client.fillPinField();
                while (true) {
                    try {
                        atm.checkPinStart(pin);
                        if (atm.getOperationStatus()) {
                            System.out.println(atm.getOperationResult());
                            break;
                        }
                        else throw new Exception();
                    } catch (Exception e) {
                        if(!atm.getOperationStatus()) {
                            System.out.println(atm.getOperationResult());
                        }
                    }
                }
                atm.getDataFromBankAccount();
                System.out.println("\n" + atm.getOperationResult() + "\n\n");


                System.out.println("Выберите действие:");
                System.out.println("1.Оплата счета\n2.Внесение наличных на счет\n3.Снятие со счета\n0.Возврат карты");
                Scanner nextMove = new Scanner(System.in);
                switch (nextMove.nextInt()) {
                    case 1 -> {
                        client.fillChecks();
                    }
                    case 2 -> {
                        client.getMoneyFromBankAccount();
                    }
                    case 3 -> {
                        client.putMoneyToBankAccount();
                    }
                    case 0 -> {
                        atm.eject();
                    }
                }

                /**
                 * TODO:
                 * обработка пин кода принято/не принято
                 * Выберените действие
                 * Оплата счето/внесение на счет/снятие со счета
                 */
            } catch (AtmException e) {
                System.out.println(e.exceptionDescription);
                if(!e.end) break;
            }
        }
    }
}
