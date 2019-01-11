import javafx.scene.control.Alert;

public class ErrorDialog {

    public static void make(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler!");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }
}
