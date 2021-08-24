import Atm.*;
import Atm.AtmException;
import Card.Card;
import Client.Client;
import com.sun.nio.sctp.SctpSocketOption;

import java.io.IOException;
import java.util.Scanner;

/**
 * Класс сценарий
 */
public class index {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Atm atm = new Atm();
        while (true) {
            try {
                //Выбор карты и ввод карты в банкомат
                Card card = client.selectCard();
                atm.newCard(card);
                System.out.println("Пожалуйста введите pin код");
                Scanner pin = new Scanner(System.in);
                while (true) {
                    //Проверка пин кода
                    try {
                        atm.checkPin(pin.nextLine());
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
                //Главное меню банкомата
                while (true) {
                    //Получение и вывод клиентских данных
                    atm.getDataFromBankAccount();
                    System.out.println("\n" + atm.getOperationResult() + "\n\n");
                    System.out.println("Выберите действие:");
                    System.out.println("1.Оплата счета\n2.Внесение наличных на счет\n3.Снятие со счета\n0.Возврат карты");
                    Scanner optionId = new Scanner(System.in);
                    switch (optionId.nextInt()) {
                        case 1 -> {
                            atm.payChecks();
                        }
                        case 2 -> {
                            client.checkWallet();
                            while (true) {
                                int added = atm.putBillsOnBankAccount(client.getSum(), client.getAvailableBills());
                                client.calculateFromWallet(added);
                                int option = new Scanner(System.in).nextInt();
                                if (option == 1) { //Комитим транзакцию и забераем деньги из клиентского кошелька
                                    client.changeWalletContains(client.getAvailableBills());
                                    atm.getMoneyBack();
                                    atm.comitTransaction("commit_add_money_transaction");
                                    break;
                                } else if (option == 3) { //Отменяем транзацию переписываем кошелек и обнуляем купюроприемник
                                    client.checkWallet();
                                    atm.getMoneyBack();
                                    atm.rollbackTransaction("rollback_add_money_transaction");
                                    break;
                                } //В наличии еще option == 2 (Добавить к транзакции) но он просто запускает след итерацию
                            }
                        }
                        case 3 -> {
                            atm.checkOutputCassette();
                            System.out.println("Снять все деньги со счета или часть?");
                            System.out.println("Внимание: банкомат может выдоавать суммы кратные 100");
                            System.out.println("1.Снять все\n2.Указать сумму\n0.Отмена");
                            Scanner optionWithdraw = new Scanner(System.in);
                            int sum = 0;
                            switch (optionWithdraw.nextInt())  {
                                case 1 -> {
                                    sum = atm.getSumFromBankAccount();
                                }
                                case 2-> {
                                    System.out.println("Пожалуйста введите сумму:");
                                    sum = new Scanner(System.in).nextInt();
                                }
                                case 3-> {
                                    continue;
                                }

                            }
                            while (true) {
                                System.out.println(sum);
                                atm.chooseSumToWithdraw(sum);
                                int option = new Scanner(System.in).nextInt();
                                break;
                                //int option = new Scanner(System.in).nextInt();
                                //atm.getBillsFromBankAccount();
                                //if (option == 1) { //Комитим транзакцию, снимаем сумму с клиента и отдаем валюту
                                    //client.changeWalletContains(client.getAvailableBills());
                                    //atm.comitGetMoneyTransaction();
                                    //atm.putBillsInDispenser();
                                //} else if (option == 3) { //Отменяем транзакцию
                                    //atm.rollbackGetMoneyTransaction();
                                //}
                            }
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
