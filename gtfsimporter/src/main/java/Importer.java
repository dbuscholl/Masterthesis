import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class Importer {
    private Connection con;
    private SuccessEvent event;

    public void setOnSuccess(SuccessEvent event) {
        this.event = event;
    }

    public void process() throws ColumnNameException, SQLException {
        con = Database.getInstancce();

        try (BufferedReader br = new BufferedReader(new FileReader(Configuration.getSelectedFile()))) {
            //first line with columnnames
            String line;
            line = br.readLine();

            processColumnNamesLine(line);

            //Other lines
            int linenumber = 2;
            while ((line = br.readLine()) != null) {
                processLine();
                linenumber++;
            }
            event.onSuccess();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void processColumnNamesLine(String line) throws SQLException, ColumnNameException {
        String[] columns = line.split(",");
        String filename = Configuration.getSelectedFile().getName();
        String tablename = filename.substring(0, filename.length() - 4);
        HashMap<String, String> types = TableConfigurations.getMap().get(filename);
        ArrayList<String> optionals = TableConfigurations.getOptionals();


        StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS " + tablename + " (");

        for (int i = 0; i < columns.length; i++) {
            // check if columnname is valid in file
            if (!types.containsKey(columns[i])) {
                throw new ColumnNameException(columns[i]);
            }

            sql.append(columns[i]);
            sql.append(" " + types.get(columns[i]));

            // Checks if it is a required field
            if (!optionals.contains(columns[i])) {      //TODO: Check if it checks object identity
                sql.append(" NOT NULL");
            }
            // Comma if there is more, ")" if not
            if (i == columns.length - 1) {
                sql.append(")");
            } else {
                sql.append(", ");
            }
        }

        Statement s = con.createStatement();
        s.execute(String.valueOf(sql));
    }

    private void processLine() {

    }

    public interface SuccessEvent {
        public void onSuccess();
    }
}
