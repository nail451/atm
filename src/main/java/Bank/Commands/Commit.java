package Bank.Commands;

import Bank.BankController;

import java.sql.SQLException;
import java.util.Map;

public class Commit implements Command {

    private int id;
    private Map<String, String> fields;

    public Commit(int id, Map<String, String> fields) {
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
        return bankController.finalTransaction(fieldsData.get("atmHash"), true);
    }
}
