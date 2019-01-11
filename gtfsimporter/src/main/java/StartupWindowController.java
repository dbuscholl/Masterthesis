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
import java.util.Arrays;
import java.util.ResourceBundle;

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
            if(portTextfield.getText().equals("")) {
                portTextfield.setText("3306");
            }
            Configuration.setSelectedFile(selectedFile);
            Configuration.setMysqlHost(hostTextfield.getText());
            Configuration.setMysqlPort(Integer.parseInt(portTextfield.getText()));
            Configuration.setMysqlUsername(usernameTextfield.getText());
            Configuration.setMysqlPassword(passwordTextfield.getText());
            Configuration.setMysqlDatabase(databaseTextField.getText());

            Importer importer = new Importer();
            importer.setOnSuccess(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Fertig!");
                alert.setHeaderText(null);
                alert.setContentText("Der Import wurde erfolgreich abgeschlossen.");
                alert.showAndWait();
            });
            try {
                importer.process();
            } catch (ColumnNameException e) {
                ErrorDialog.make("Spaltennamen sind unzulässig. Bitte mit der GTFS Dokumentation prüfen! Spalte: " + e.getMessage());
            } catch (SQLException e) {
                ErrorDialog.make("Datenbankverbindung konnte nicht hergestellt werden. Bitte Verbindungsdaten überprüfen!");
            }
        });
    }
}
