package Atm;

import Card.Card;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Класс описывающий картоприемник
 */
class CardAcceptor implements Receiving {
    private Card insertedCard;
    private boolean cardInside;

    private String accountNumber;
    private String expirationDate;
    private String holder;

    /**
     * Конструктор класса,
     * Получает карту и данные с нее
     * @param newCard Card
     */
    public CardAcceptor(Card newCard) {
        insertedCard = newCard;
        receive();
        cardInside = isValidCard(newCard);
    }

    /// get+set ///

    public boolean getCardInside() {
        return cardInside;
    }

    private void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber(){
        return accountNumber;
    }

    /**
     * Сеттер поля expirationDate
     * Переводит дату хранящуюся на карте в unix_timestamp
     * @param expirationDate Строка с датой
     */
    private void setExpirationDate(String expirationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(expirationDate);
            this.expirationDate = parsedDate.getTime()/1000 + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getExpirationDate(){
        return expirationDate;
    }

    private void setHolder(String holder) {
        this.holder = holder;
    }

    public String getHolder(){
        return holder;
    }

    /// get+set ///

    /**
     * Реализация метода из интрефейса Receiving
     * Получет данные с карты и раскладывает их по
     * соотвествующим полям
     */
    @Override
    public void receive() {
        assert insertedCard != null;
        List<String> insertedCardData = insertedCard.getCardData();
        for(String line : insertedCardData) {
            if (line.length() == 16 && isNumeric(line)) setAccountNumber(line);
            if (line.contains("/")) setExpirationDate(line);
            if (line.contains(" ")) setHolder(line);
        }
    }

    /**
     * Вывод карты
     */
    public void cardEject() throws AtmException {
        insertedCard = null;
        cardInside = false;
        throw new AtmException("Заберите карту\n\n", true);
    }

    /**
     * Проверка карты на валидность
     * проверяет по маске значиеня на карте
     * @param newCard объект класса Card
     * @return boolean
     */
    private boolean isValidCard(Card newCard){
        boolean isValid = false;
        if(Objects.nonNull(accountNumber) && isNumeric(accountNumber) && accountNumber.length() == 16) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Проверяет строку на полное содержание цифр
     * @param str строка на проверку
     * @return boolean результат проверки
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int strSize = str.length();
        for (int i = 0; i < strSize; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}