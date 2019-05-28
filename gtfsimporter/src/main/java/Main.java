import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This is the Main Class, the first starting point of the application. As soon as the user opens the Jar file resp.
 * launches the application, this class with this main method runs. <p><b>Consider running this programm from console to get
 * progress information on importing as this can take some time. Took about 15 minutes to import stop_times lately.</b></p>
 */
public class Main extends Application {
    private static Stage primaryStage;

    /**
     * launch invokes the start Method after main method. Therefore this is only a delegat to start().
     * @param args cmd args
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * this is the actual start function where the main window is being build and shown. This is done by reading the
     * corresponding layout file and setting it as scene for the window. The window gets a fixed non resizeable width and
     * height and the title "GTFS MySQL Importer".
     * @param primaryStage
     * @throws Exception
     */
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("startupWindow.fxml"));

        Scene scene = new Scene(root, 300, 275);

        primaryStage.setTitle("GTFS MySQL Importer");
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.show();

        setPrimaryStage(primaryStage);
    }

    /**
     * returns the primary stage
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * setter
     * @param primaryStage the primary stage
     */
    public static void setPrimaryStage(Stage primaryStage) {
        Main.primaryStage = primaryStage;
    }
}
