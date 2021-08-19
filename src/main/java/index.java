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
                Scanner pin = client.fillPinField();
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
                            System.out.println(atm.getOperationResult());
                        }
                    }
                }
                //Получение клиентских данных
                atm.getDataFromBankAccount();
                System.out.println("\n" + atm.getOperationResult() + "\n\n");

                //Главное меню банкомата
                while (true) {
                    atm.giveAnOptions();
                    Scanner optionId = client.chooseOption();
                    switch (optionId.nextInt()) {
                        case 1 -> {
                            atm.payChecks();
                        }
                        case 2 -> {
                            while (true) {
                                atm.putBillsOnBankAccount();
                                int option = client.chooseOption().nextInt();
                                if (option == 1) {
                                    atm.endTransaction();
                                    break;
                                } else if (option == 3) {
                                    atm.getMoneyBack();
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }
                        case 3 -> {
                            atm.getMoneyFromBankAccount();
                        }
                        case 0 -> {
                            atm.eject();
                        }
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
