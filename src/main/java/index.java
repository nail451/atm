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
                //Выбор карты и ввод карты в банкомат
                Card card = client.selectCard();
                client.putCardInCardAcceptor(atm, card);
                atm.checkPinAskPin();
                Scanner pin = client.enterAnswer();
                while (true) {
                    //Проверка пин кода
                    try {
                        atm.checkPinStart(pin);
                        if (atm.getOperationStatus()) {
                            System.out.println(atm.getOperationResult());
                            break;
                        }
                        else throw new Exception();
                    } catch (Exception e) {
                        if(!atm.getOperationStatus()) {
                            if (atm.isOperationCritical()) {
                                atm.eject();
                            }
                            System.out.println(atm.getOperationResult());
                        }
                    }
                }
                //Получение и вывод клиентских данных
                atm.getDataFromBankAccount();
                System.out.println("\n" + atm.getOperationResult() + "\n\n");

                //Главное меню банкомата
                while (true) {
                    atm.giveOptions();
                    Scanner optionId = client.enterAnswer();
                    switch (optionId.nextInt()) {
                        case 1 -> {
                            atm.payChecks();
                        }
                        case 2 -> {
                            while (true) {
                                client.checkWallet();
                                int added = atm.putBillsOnBankAccount(client.getSum(), client.getAvailableBills());
                                client.calculateFromWallet(added);
                                int option = client.enterAnswer().nextInt();
                                if (option == 1) {
                                    atm.endTransaction();
                                    break;
                                } else if (option == 3) {
                                    atm.getMoneyBack();
                                    break;
                                }
                            }
                        }
                        case 3 -> {
                            atm.getMoneyFromBankAccount();
                        }
                        case 0 -> atm.eject();
                    }
                }
            } catch (AtmException e) {
                System.out.println(e.exceptionDescription);
                if(!e.end) break;
            }
        }
    }
}
