package Atm;

import Card.Card;

import java.util.Objects;


public class CardAcceptor extends Atm{
    private String cardNumber;
    private boolean cardCheck;

    public boolean getCardCheck() {
        return cardCheck;
    }

    public CardAcceptor(Card newCard) {
        if(isValidCard(newCard)) {
            cardNumber = newCard.getAccountNumber();
            cardCheck = true;
        }
    }

    public static void cardEject(){

    }

    private boolean isValidCard(Card newCard){
        boolean isValid = false;
        if(Objects.nonNull(newCard.getAccountNumber())) {
            isValid = true;
        }
        return isValid;
    }
}
