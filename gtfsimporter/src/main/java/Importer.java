import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class Importer {
    private Connection con;
    private SuccessEvent event;
    private String[] columns;
    private ArrayList<String> warnings;

    public void setOnSuccess(SuccessEvent event) {
        this.event = event;
    }

    public void process() throws ColumnNameException, SQLException, IOException {
        con = Database.getInstancce();
        warnings = new ArrayList<>();
        String filename = Configuration.getSelectedFile().getName();
        int linesTotal = countLines(Configuration.getSelectedFile().getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(Configuration.getSelectedFile()))) {
            //first line with columnnames
            String line;
            line = br.readLine();

            processColumnNamesLine(line);

            //Other lines


            String tablename = filename.substring(0, filename.length() - 4);
            StringBuffer query = new StringBuffer("INSERT INTO " + tablename + " (" + String.join(", ", columns) + ") VALUES (");
            for (int i = 0; i < columns.length; i++) {
                if (i == columns.length - 1) {
                    query.append("?)");
                } else {
                    query.append("?,");
                }
            }
            PreparedStatement s = con.prepareStatement(query.toString());

            int linenumber = 2;
            while ((line = br.readLine()) != null) {
                processLine(line, linenumber, s);
                linenumber++;
                if (linenumber % 250 == 0 || linenumber == linesTotal) {
                    s.executeBatch();
                    // TODO: show in JavaFX Window
                    System.out.println("Done: " + (((double) (int) (((double) linenumber / (double) linesTotal) * 10000) / 100)) + "%");
                }
            }

            event.onSuccess(warnings);
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
                System.out.println("Could not find column '" + columns[i] + "'. Check file for illegal characters. Also try different charsets.");
                throw new ColumnNameException(columns[i]);
            }

            sql.append(columns[i]);
            sql.append(" " + types.get(columns[i]));

            // Checks if it is a required field
            if (!optionals.contains(columns[i])) {
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

        this.columns = columns;
    }

    private void processLine(String line, int linenumber, PreparedStatement s) throws SQLException {
        ArrayList<String> optionals = TableConfigurations.getOptionals();
        StringBuffer value = new StringBuffer();


        int item = 1;

        for (int i = 0; i < line.length(); i++) {
            // if a " was escaped
            if (line.charAt(i) == '"') {
                // if value end
                if (i == line.length() - 1 || line.charAt(i + 1) == ',') {
                    // type dependent assignment
                    String type = TableConfigurations.getMap().get(Configuration.getSelectedFile().getName()).get(columns[item - 1]);
                    switch (type) {
                        case "integer":
                            try {
                                s.setInt(item, Integer.parseInt(value.toString()));
                            } catch (NumberFormatException e) {
                                warnings.add("Keine Gültige Zahl: " + value.toString() + " für " + columns[item - 1]);
                                i++;
                                item++;
                                value.setLength(0);
                                continue;
                            }
                            break;
                        default:
                            s.setString(item, value.toString());
                    }

                    // reset and increase
                    item++;
                    i++; // skip comma or else there will be a value with only a comma
                    value.setLength(0);
                }
                if (value.length() > 0) {
                    if (line.charAt(i + 1) == '"') {
                        i++; // else there will be two "
                        continue;
                    }
                }
            } else {
                value.append(line.charAt(i));
            }
        }
        // run query
        s.addBatch();
    }

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public interface SuccessEvent {
        public void onSuccess(ArrayList<String> warnings);
    }
}
