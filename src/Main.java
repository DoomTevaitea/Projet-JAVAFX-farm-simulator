import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/farm.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Farmer Simulator");
        stage.setScene(scene);
        stage.setMinWidth(995);   // largeur minimale
        stage.setMinHeight(832);  // hauteur minimale
        stage.setMaximized(true); // démarre en plein écran
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
