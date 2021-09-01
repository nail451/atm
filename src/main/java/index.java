import Atm.*;
import Atm.AtmException;
import Card.Card;
import Client.Client;
import java.io.IOException;
import java.util.Scanner;

public class index {
    public static void main(String[] args) throws IOException {
        Client client = Client.getInstance();
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
                    System.out.println("1.Внесение наличных на счет\n2.Снятие со счета\n3.Оплата счета\n0.Возврат карты");
                    Scanner optionId = new Scanner(System.in);
                    String id = optionId.nextLine();
                    if(id.equals("1")) {
                        client.checkWallet();
                        while (true) {
                            int added = atm.putBillsOnBankAccount(client.getSum(), client.getAvailableBills());
                            if(added == 0) break;
                            System.out.println(client.getAvailableBills());
                            client.calculateFromWallet(added);
                            System.out.println(client.getAvailableBills());
                            String option = new Scanner(System.in).nextLine();
                            if (option.equals("1")) { //Комитим транзакцию и забераем деньги из клиентского кошелька
                                if(client.changeWalletContains(client.getAvailableBills())){
                                    System.out.println("Деньги изъяты  из кошелька");
                                }
                                atm.getMoneyBack();
                                atm.commit();
                                break;
                            } else if (option.equals("3")) { //Отменяем транзацию переписываем кошелек и обнуляем купюроприемник
                                client.checkWallet();
                                atm.getMoneyBack();
                                atm.rollback();
                                break;
                            } //В наличии еще option == 2 (Добавить к транзакции) но он просто запускает след итерацию
                        }
                    } else if(id.equals("2")) {
                        atm.checkOutputCassette();
                        while (true) {
                            System.out.println("\n\nСнять все деньги со счета или часть?");
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
                                    continue;
                                }
                                if(atm.askCommit()) {
                                    client.putBillsInTheWallet(atm.giveMoney());
                                    atm.commit();
                                } else {
                                    atm.rollback();
                                    continue;
                                }
                                break;
                            }
                        }
                    } else if(id.equals("3")) {
                        atm.payChecks();
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