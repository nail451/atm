import Atm.*;
import Atm.AtmException;
import Card.Card;
import Client.Client;
import java.io.IOException;
import java.util.Scanner;

public class index {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        InteractiveAtm atm = Atm.getInstance();
        while (true) {
            try {
                //Выбор карты и ввод карты в банкомат
                Card card = client.selectCard();
                atm.getCard(card);
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
                    if(!atm.getOperationStatus()) {
                        System.out.println(atm.getOperationResult());
                        atm.eject();
                    }
                    System.out.println("\n" + atm.getOperationResult() + "\n\n");
                    System.out.println("Выберите действие:");
                    System.out.println("1.Оплата счета\n2.Внесение наличных на счет\n3.Снятие со счета\n0.Возврат карты");
                    Scanner optionId = new Scanner(System.in);
                    int id = optionId.nextInt();
                    if(id == 1) {
                        atm.payChecks();
                    } else if(id == 2) {
                        client.checkWallet();
                        while (true) {
                            int added = atm.putBillsOnBankAccount(client.getSum(), client.getAvailableBills());
                            if(added == 0) break;
                            System.out.println(client.getAvailableBills());
                            client.calculateFromWallet(added);
                            System.out.println(client.getAvailableBills());
                            int option = new Scanner(System.in).nextInt();
                            if (option == 1) { //Комитим транзакцию и забераем деньги из клиентского кошелька
                                client.changeWalletContains(client.getAvailableBills());
                                atm.getMoneyBack();
                                atm.commit();
                                break;
                            } else if (option == 3) { //Отменяем транзацию переписываем кошелек и обнуляем купюроприемник
                                client.checkWallet();
                                atm.getMoneyBack();
                                atm.rollback();
                                break;
                            } //В наличии еще option == 2 (Добавить к транзакции) но он просто запускает след итерацию
                        }
                    } else if(id == 3) {
                        atm.checkOutputCassette();
                        while (true) {
                            System.out.println("Снять все деньги со счета или часть?");
                            System.out.println("Внимание: банкомат может выдоавать только суммы кратные 100");
                            System.out.println("1.Снять все\n2.Указать сумму\n0.Отмена");
                            Scanner optionWithdraw = new Scanner(System.in);
                            int option = optionWithdraw.nextInt();
                            int sum = 0;
                            if(option == 1) {
                                sum = atm.getSumFromBankAccount();
                            } else if(option == 2) {
                                    System.out.println("Пожалуйста введите сумму:");
                                    sum = new Scanner(System.in).nextInt();
                            } else {
                                break;
                            }
                            if(sum != 0) {
                                try {
                                    atm.chooseSumToWithdraw(sum);
                                } catch (Error e) {
                                    break;
                                }
                                if(atm.askCommit()) {
                                    atm.commit();
                                } else {
                                    atm.rollback();
                                }
                                break;
                            } else {
                                break;
                            }
                        }
                    } else {
                        atm.eject();
                    }
                }
            } catch (AtmException e) {
                System.out.println(e.getExceptionDescription());
                if(!e.getEnd()) break;
            }
        }
    }
}