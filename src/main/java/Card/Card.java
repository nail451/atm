package Card;

public class Card {
    private int pin;
    private String accountNumber;
    private String expirationDate;
    private String accountHolderName;
    private String accountHolderSoname;

    ///// Блок get/set /////

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getAccountHolderName() {
        return accountHolderName + " " + accountHolderSoname;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public void setAccountHolderSoname(String accountHolderSoname) {
        this.accountHolderSoname = accountHolderSoname;
    }

    @Override
    public String toString() {
        return getAccountNumber() + "/" + getAccountHolderName() + " " + getExpirationDate();
    }
}
