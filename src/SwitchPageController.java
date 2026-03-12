import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SwitchPageController {

    @FXML
    private Button ShopView;

    @FXML
    private Button InventoryView;

    @FXML
    private Button FieldView;

    @FXML
    private Button Home;

    @FXML
    private Button Save;


    @FXML
    public void showShop() {
        // Ajouter un action listener
        InventoryView.setOnAction(event -> {

        });
    }
}
