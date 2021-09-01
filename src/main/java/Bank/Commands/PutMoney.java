package Bank.Commands;

import Bank.BankController;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PutMoney implements Command {

    private int id;
    private Map<String, String> fields;

    public PutMoney(int id, Map<String, String> fields) {
        this.id = id;
        setFields(fields);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    @Override
    public Map<String, String> retrieveResult() throws SQLException {
        Map<String, String> fieldsData = getFields();
        BankController bankController = BankController.getInstance();
        Map<String, String> response = new HashMap<>();
        try {
            return bankController.makeTransaction(
                    fieldsData.get("atmHash"),
                    bankController.makeMoneyQuery(
                            fieldsData.get("atmHash"),
                            fieldsData.get("sum"),
                            fieldsData.get("accountNumber"),
                            "put"));
        } catch (SQLException s) {
            response.put("status", "false");
            return response;
        }
    }
}
