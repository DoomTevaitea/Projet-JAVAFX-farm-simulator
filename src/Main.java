import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Chargement du fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/farm.fxml"));
        Scene scene = new Scene(loader.load());

        // Configuration de la fenêtre
        stage.setTitle("Farmer Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
