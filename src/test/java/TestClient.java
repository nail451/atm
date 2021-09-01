import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TestClient {

    int sum;
    Map<String, String> availableBills;

    private void setSum(int sum) {
        this.sum = sum;
    }

    public int getSum() { return sum; }

    public Map<String, String> getAvailableBills() { return availableBills; }

    private void setAvailableBills(Map<String, String> availableBills ) {
        this.availableBills = availableBills;
    }


    public TestClient(){
        Map<String,String> result = getBillFromWallet();
        setSumWallet(result);
        setAvailableBills(result);
    }

    @Test
    public void testAddingToWallet(){
        System.out.println("Start");
        Map<String, String> money = new HashMap<>();
        money.put("5000", "2");
        putBillsInTheWallet(money);
        System.out.println("end");

        System.out.println("Start");
        Map<String, String> money2 = new HashMap<>();
        money2.put("100", "5");
        putBillsInTheWallet(money2);
        System.out.println("end");

        System.out.println("Start");
        Map<String, String> money3 = new HashMap<>();
        money3.put("5000", "2");
        money3.put("500", "2");
        putBillsInTheWallet(money3);
        System.out.println("end");

        System.out.println("Start");
        Map<String, String> money4 = new HashMap<>();
        money4.put("5000", "2");
        money4.put("1000", "2");
        money4.put("500", "2");
        money4.put("100", "2");
        putBillsInTheWallet(money4);
        System.out.println("end");
    }

    private void setSumWallet(Map<String, String> wallet) {
        List<String> bills = new LinkedList<>(wallet.keySet());
        bills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        int sum = 0;
        for (String i : bills) {
            sum +=  Integer.parseInt(i) * Integer.parseInt(wallet.get(i));
        }
        setSum(sum);
    }

    public void putBillsInTheWallet(Map<String, String> money) {
        Map<String,String> moneyInWallet = getAvailableBills();
        List<String> bills = new LinkedList<>(moneyInWallet.keySet());
        bills.sort((Comparator) (o1, o2) -> Integer.parseInt((String) o1) > Integer.parseInt((String)o2) ? -1 : 1);
        for(String bill : bills) {
            if(money.containsKey(bill)) {
                int amountIn = Integer.parseInt(money.get(bill));
                int amountWhole = Integer.parseInt(moneyInWallet.get(bill));
                moneyInWallet.put(bill, String.valueOf(amountIn+amountWhole));
            }
        }
        System.out.println(moneyInWallet);
    }

    private Map<String, String> getBillFromWallet(){
        File wallet = new File("src/main/java/Client/wallet");
        Scanner walletData = null;
        try {
            walletData = new Scanner(wallet);
        } catch (FileNotFoundException e) {
            System.out.println("Кошелька нет, украли чтоли?");
        }
        String data = "";
        assert walletData != null;
        if (walletData.hasNextLine()) {
            data = walletData.nextLine();
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Map<String, String> result = gson.fromJson(data, HashMap.class);
        return  result;
    }
}
