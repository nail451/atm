package Bank.Commands;

import java.sql.SQLException;
import java.util.Map;

public interface Command {
    Map<String,String> retrieveResult() throws SQLException;
}

