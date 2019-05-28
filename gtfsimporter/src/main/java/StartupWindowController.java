import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * <p>This is the main view of the Application. It's contents are the textfields where the user can enter alls information
 * about the database and select a file to be imported via filepath chooser. It also contains a start button which triggers
 * all the magic.</p>
 * <p>The initalization method assigns all listeners to the important viwes. These are the filechooser which only should
 * search for textfiles and the start button which starts all the majesty. After click the programm reads out all the
 * typed in values from the textfields and sets them into the configuration class. Then the importer is starting and
 * doing work. After that, the window has a listener for that, it shows a success dialog or a error dialog corresponding
 * to the outcome of the import.</p>
 */
public class StartupWindowController implements Initializable {
    private static File selectedFile = null;

    @FXML
    private TextField filepathTextfield;
    @FXML
    private Button filepathChooserButton;
    @FXML
    private TextField hostTextfield;
    @FXML
    private TextField portTextfield;
    @FXML
    private TextField usernameTextfield;
    @FXML
    private TextField passwordTextfield;
    @FXML
    private TextField databaseTextField;
    @FXML
    private Button startButton;


    public void initialize(URL location, ResourceBundle resources) {
        filepathChooserButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File f = fileChooser.showOpenDialog(Main.getPrimaryStage());
            if (f != null) {
                if (Arrays.asList(Configuration.filenames).indexOf(f.getName()) == -1) {
                    StringBuffer s = new StringBuffer("Dateiname ungültig. Folgende Dateien sind erlaubt: \n");
                    for (int i = 0; i < Configuration.filenames.length; i++) {
                        s.append(Configuration.filenames[i] + "\n");
                    }
                    ErrorDialog.make(s.toString());
                }
                selectedFile = f;

                filepathTextfield.setText(f.getAbsolutePath());
            }
        });

        startButton.setOnAction(event -> {
            if (portTextfield.getText().equals("")) {
                portTextfield.setText("3306");
            }
            Configuration.setSelectedFile(selectedFile);
            Configuration.setMysqlHost(hostTextfield.getText());
            Configuration.setMysqlPort(Integer.parseInt(portTextfield.getText()));
            Configuration.setMysqlUsername(usernameTextfield.getText());
            Configuration.setMysqlPassword(passwordTextfield.getText());
            Configuration.setMysqlDatabase(databaseTextField.getText());

            Importer importer = new Importer();
            importer.setOnSuccess((ArrayList<String> warnings) -> {
                if (warnings.size() == 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Fertig!");
                    alert.setHeaderText(null);
                    alert.setContentText("Der Import wurde erfolgreich abgeschlossen.");
                    alert.showAndWait();
                } else {
                    StringBuffer text = new StringBuffer("Mit Fehlern abgeschlossen:\n");
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Fertig!");
                    alert.setHeaderText(null);
                    for (String warning : warnings)
                        text.append(warning + "\n");
                    alert.setContentText(text.toString());
                    alert.showAndWait();
                }
            });
            try {
                importer.process();
            } catch (ColumnNameException e) {
                ErrorDialog.make("Spaltennamen sind unzulässig. Bitte mit der GTFS Dokumentation prüfen! Spalte: " + e.getMessage());
            } catch (SQLException e) {
                ErrorDialog.make("Datenbankverbindung konnte nicht hergestellt werden. Bitte Verbindungsdaten überprüfen!");
            } catch (IOException e) {
                ErrorDialog.make("Fehler bei der Ermittlung der Dateilänge");
            }
        });
    }
}
