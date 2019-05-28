import javafx.scene.control.Alert;

/**
 * a custom widget which will be displayed when something went wrong during any process of the whole application.
 */
public class ErrorDialog {

    /**
     * Creates the Error Dialog with an Error Icon and "Fehler" as error message without any header.
     * @param text the text which should be displayed inside the dialog in addition to "Fehler!".
     */
    public static void make(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler!");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }
}
