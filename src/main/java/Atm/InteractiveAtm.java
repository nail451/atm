package Atm;

import Card.Card;

import java.util.Map;

public interface InteractiveAtm {

    void getCard(Card card);

    void checkPin(String pinCode) throws Exception;

    boolean getOperationStatus();

    boolean isOperationCritical();

    String getOperationResult();

    void eject() throws AtmException;

    void getDataFromBankAccount() throws AtmException;

    void payChecks();

    int putBillsOnBankAccount(int sum, Map<String, String> billsAvailable) throws AtmException;

    boolean askCommit();

    void commit() throws  AtmException;

    void rollback() throws  AtmException;

    void getMoneyBack();

    void checkOutputCassette();

    int getSumFromBankAccount() throws AtmException;

    void chooseSumToWithdraw(int sum) throws AtmException;

}
