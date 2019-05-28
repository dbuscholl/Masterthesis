import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *This class is the actual importer process.
 * <p>The main method of this class is process() where everything starts from. After getting a database instance the
 * importer first processes the column names line which is the first one and creates the database table. To know which
 * datatypes it should use, it takes use of the TableConfigurations class.</p>
 * <p>Then it starts the import of the actual data. Row by row the importer parses each character and checks for commas
 * which are the sperarators of the fields. But as a comma can also be part of the actual value the importer checks the
 * char after the comma. If it is a quotation marks then the importer know that a new field starts. All chars which were
 * collected until then are concatenated into a string and stored into a temporal array until the importer got all strings
 * for the row. This is needed because you need to insert all values of a row in mysql inside the INSERT statement. If the
 * next char is anything else then the importers ignores it and adds it to the string. </p>
 * <p>This class makes also use of batch insertion for prepared statements to improve performance. This means that every
 * 250 rows all of them are imported once and not every row on its own. But the import process can still take a lot of time
 * especially for the stop_times table, I measured 14 minutes lately.</p>
 * <p><b>Consider running this programm from console to get
 * progress information on importing as this can take some time. Took about 15 minutes to import stop_times lately.</b></p>
 */
public class Importer {
    private Connection con;
    private SuccessEvent event;
    private String[] columns;
    private ArrayList<String> warnings;

    /**
     * sets a callback for when the importer is done with its magic
     * @param event the function to be called
     */
    public void setOnSuccess(SuccessEvent event) {
        this.event = event;
    }

    /**
     * the entry point function. It starts the importing process. Creating the Table and importing actual data for a single
     * row are implemented in separate functions. However this function takes care of console output.
     * @throws ColumnNameException when something goes wrong with the tablenames
     * @throws SQLException when something else with the database goes wrong e.g. wrong statements
     * @throws IOException when something during the reading process of the textfile goes wrong e.g. file gets deleted
     */
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
                processLine(line, s);
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

    /**
     * this function processes the column names and creates the table by using all information from TableConfigurations class
     * @param line the line as string where all table names are listed inside the textfile
     * @throws SQLException when something else with the database goes wrong e.g. wrong statements
     * @throws ColumnNameException when something goes wrong with the tablenames
     */
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

    /**
     * This function is for actually processing a line containing fields with content for the database. The whole process
     * how this is done is described in class description above.
     * @param line the line should be parsed
     * @param s the statement to which the row should be added to for batch execution
     * @throws SQLException  when something else with the database goes wrong e.g. wrong statements
     */
    private void processLine(String line, PreparedStatement s) throws SQLException {
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

    /**
     * this function counts all lines inside the textfield to display progress information inside the console.
     * @param filename the filename of which the lines should be count
     * @return the amount of lines inside the file
     * @throws IOException when something goes wrong during reading e.g. file gets deleted.
     */
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

    /**
     * Callback Interface for the completion of importing all values.
     */
    public interface SuccessEvent {
        void onSuccess(ArrayList<String> warnings);
    }
}
