package Bank.Commands;

import Bank.BankController;
import java.sql.SQLException;
import java.util.Map;

public class GetData implements Command {

    private int id;
    private Map<String, String> fields;

    public GetData(int id, Map<String, String> fields) {
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
        return bankController.getUserByCardData(fieldsData.get("accountNumber"), true);
    }
}
