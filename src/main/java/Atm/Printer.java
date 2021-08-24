package Atm;

/**
 * Класс отвечающий за формирование и выдачу чеков
 */
class Printer implements Issuing {
    private int receiptPaper;
    private boolean receiptPaperError;

    public void createNewReceipt(){
    }

    public void print(String command, String atmHash, String cardNumber, String sum) {

    }

    @Override
    public String toString() {
        return "";
    }
}