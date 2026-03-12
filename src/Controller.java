import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Controller {

    // =====================================================================
    // FXML
    // =====================================================================
    @FXML private VBox     ShopPage, InventoryPage, HomePage, StatisticPage;
    @FXML private Label    DollarsView, AlertLabel, MeteoLabel;
    @FXML private GridPane farmGrid;

    // =====================================================================
    // MODELE
    // =====================================================================
    private Player   player = new Player("tevaitea");
    private Shop     shop   = new Shop(player.getInventory(), player.getWallet());
    private Meteo    meteo  = new Meteo();
    private Plot[][] plots  = new Plot[15][5];

    private Item selectedItem  = null;
    private int  totalRevenues = 0;
    private int  totalDepenses = 0;
    private int  totalRecoltes = 0;

    private Timeline alertTimeline;

    // CSS
    private static final String BTN_VERT    = "-fx-background-color: #4a7c2f; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 14;";
    private static final String BTN_ROUGE   = "-fx-background-color: #cc0000; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 14;";
    private static final String BTN_MARRON  = "-fx-background-color: #8B6914; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 14;";
    private static final String PLOT_VIDE   = "-fx-background-color: #514e4b;";
    private static final String PLOT_LIBRE  = "-fx-background-color: #c8a060;";
    private static final String PLOT_ANIMAL = "-fx-background-color: #a0522d;";
    private static final String PLOT_PRET   = "-fx-background-color: #cc0000;";
    private static final String FEED_NORMAL = "-fx-background-color: #8B6914; -fx-text-fill: white; -fx-font-size: 10px; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 4;";
    private static final String FEED_ROUGE  = "-fx-background-color: #cc0000; -fx-text-fill: white; -fx-font-size: 10px; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 4;";

    // =====================================================================
    // INITIALISATION
    // =====================================================================
    @FXML
    public void initialize() {
        createPlots();
        createShopUI();
        updateWalletDisplay();
        updateMeteoDisplay();
        meteo.demarrerCycle(() -> updateMeteoDisplay());
        loadGame();
    }

    // =====================================================================
    // NAVIGATION
    // =====================================================================
    @FXML public void showHome() {
        HomePage.setVisible(true);
        ShopPage.setVisible(false);
        InventoryPage.setVisible(false);
        StatisticPage.setVisible(false);
    }

    @FXML public void showShop() {
        ShopPage.setVisible(true);
        HomePage.setVisible(false);
        InventoryPage.setVisible(false);
        StatisticPage.setVisible(false);
    }

    @FXML public void showInventory() {
        InventoryPage.setVisible(true);
        ShopPage.setVisible(false);
        HomePage.setVisible(false);
        StatisticPage.setVisible(false);
        createInventoryUI();
    }

    @FXML public void showStatistic() {
        StatisticPage.setVisible(true);
        ShopPage.setVisible(false);
        InventoryPage.setVisible(false);
        HomePage.setVisible(false);
        createStatisticUI();
    }

    // =====================================================================
    // ALERTES
    // =====================================================================
    private void showAlert(String message) {
        AlertLabel.setText(message);
        if (alertTimeline != null) alertTimeline.stop();
        alertTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> AlertLabel.setText("")));
        alertTimeline.setCycleCount(1);
        alertTimeline.play();
    }

    // =====================================================================
    // WALLET + METEO
    // =====================================================================
    private void updateWalletDisplay() {
        DollarsView.setText(player.getWallet().getDollars() + " $");
    }

    private void updateMeteoDisplay() {
        MeteoLabel.setText("Meteo : " + meteo.getDescription());
    }

    // =====================================================================
    // RESET
    // =====================================================================
    @FXML
    public void resetGame() {
        player.reset();
        selectedItem  = null;
        totalRevenues = 0;
        totalDepenses = 0;
        totalRecoltes = 0;

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 5; j++) {
                plots[i][j] = new Plot(true);
                Button btn     = (Button) farmGrid.lookup("#terrain_" + i + "_" + j);
                Button feedBtn = (Button) farmGrid.lookup("#feed_"    + i + "_" + j);
                if (btn     != null) { btn.setStyle(PLOT_VIDE); btn.setText(""); }
                if (feedBtn != null)   feedBtn.setVisible(false);
            }
        }

        try {
            Files.deleteIfExists(Path.of("save.json"));
        } catch (IOException e) {
            System.out.println("Erreur suppression save : " + e.getMessage());
        }

        updateWalletDisplay();
        showHome();
        showAlert("Partie réinitialisée !");
    }

    // =====================================================================
    // INVENTORY - UI
    // =====================================================================
    private void createInventoryUI() {
        InventoryPage.getChildren().clear();
        InventoryPage.setPadding(new Insets(0));
        InventoryPage.setSpacing(0);

        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(20));
        scrollContent.setStyle("-fx-background-color: #f5f0e8;");

        Label titre = new Label("Inventaire");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d5a1b;");
        scrollContent.getChildren().add(titre);

        if (selectedItem != null) {
            Label selectionLabel = new Label("Sélectionné : " + selectedItem.getName());
            selectionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a7c2f;");
            scrollContent.getChildren().add(selectionLabel);
        }

        boolean hasSeeds    = false;
        boolean hasAnimals  = false;
        boolean hasCrops    = false;
        boolean hasProducts = false;

        for (Item item : player.getInventory().getItems().values()) {
            if (item instanceof Item.Seed)          hasSeeds    = true;
            if (item instanceof Item.Animal)        hasAnimals  = true;
            if (item instanceof Item.Crop)          hasCrops    = true;
            if (item instanceof Item.AnimalProduct) hasProducts = true;
        }

        if (hasSeeds) {
            scrollContent.getChildren().add(makeInventorySectionTitle("Graines"));
            scrollContent.getChildren().add(buildInventoryGrid(Item.Seed.class));
        }
        if (hasAnimals) {
            scrollContent.getChildren().add(makeInventorySectionTitle("Animaux"));
            scrollContent.getChildren().add(buildInventoryGrid(Item.Animal.class));
        }
        if (hasCrops) {
            scrollContent.getChildren().add(makeInventorySectionTitle("Récoltes"));
            scrollContent.getChildren().add(buildInventoryGrid(Item.Crop.class));
        }
        if (hasProducts) {
            scrollContent.getChildren().add(makeInventorySectionTitle("Produits animaux"));
            scrollContent.getChildren().add(buildInventoryGrid(Item.AnimalProduct.class));
        }

        if (!hasSeeds && !hasAnimals && !hasCrops && !hasProducts) {
            Label vide = new Label("Votre inventaire est vide !");
            vide.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");
            scrollContent.getChildren().add(vide);
        }

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(668);
        scrollPane.setStyle("-fx-background: #f5f0e8; -fx-background-color: #f5f0e8; -fx-border-color: transparent;");

        InventoryPage.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private GridPane buildInventoryGrid(Class<?> type) {
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15); grid.setPadding(new Insets(5));
        int col = 0;
        for (Item item : player.getInventory().getItems().values()) {
            if (type.isInstance(item)) {
                grid.add(createInventoryCard(item), col++, 0);
            }
        }
        return grid;
    }

    private VBox createInventoryCard(Item item) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(160);

        if (item.isSelected()) {
            card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #4a7c2f; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4;");
        } else {
            card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        }

        Label emojiLabel = new Label(getEmoji(item));
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label qtyLabel = new Label("Quantité : " + item.getQuantity());
        qtyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a7c2f;");

        card.getChildren().addAll(emojiLabel, nameLabel, qtyLabel);

        if (item instanceof Item.Animal) {
            Item.Animal animal = (Item.Animal) item;
            Label foodLabel = new Label("Nourrir avec : " + animal.getFoodRequired());
            foodLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8B6914;");
            foodLabel.setWrapText(true);
            foodLabel.setMaxWidth(140);
            card.getChildren().add(foodLabel);
        }

        Button selectBtn = new Button("Sélectionner");
        selectBtn.setStyle("-fx-background-color: #4a7c2f; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        selectBtn.setOnAction(e -> {
            if (selectedItem != null) {
                selectedItem.setSelected(false);
            }
            item.setSelected(true);
            selectedItem = item;
            createInventoryUI();
        });

        cancelBtn.setOnAction(e -> {
            item.setSelected(false);
            if (selectedItem == item) {
                selectedItem = null;
            }
            createInventoryUI();
        });

        card.getChildren().addAll(selectBtn, cancelBtn);
        return card;
    }

    private Label makeInventorySectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        return lbl;
    }

    // =====================================================================
    // SHOP - UI
    // =====================================================================
    private void createShopUI() {
        ShopPage.getChildren().clear();
        ShopPage.setPadding(new Insets(0));
        ShopPage.setSpacing(0);

        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(20));
        scrollContent.setStyle("-fx-background-color: #f5f0e8;");

        Label titreAcheter = new Label("Acheter");
        titreAcheter.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d5a1b;");
        scrollContent.getChildren().add(titreAcheter);
        scrollContent.getChildren().add(makeSectionTitle("Graines"));
        scrollContent.getChildren().add(makeItemGrid(shop.getBuyItems(), Item.Seed.class, true));
        scrollContent.getChildren().add(makeSectionTitle("Animaux"));
        scrollContent.getChildren().add(makeItemGrid(shop.getBuyItems(), Item.Animal.class, true));

        Label sep = new Label("─────────────────────────────────────────────────────────────────────");
        sep.setStyle("-fx-text-fill: #aaaaaa;");
        scrollContent.getChildren().add(sep);

        Label titreVendre = new Label("Vendre");
        titreVendre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d5a1b;");
        scrollContent.getChildren().add(titreVendre);
        scrollContent.getChildren().add(makeSectionTitle("Graines et Animaux"));
        scrollContent.getChildren().add(makeItemGrid(shop.getBuyItems(), Item.Seed.class, false));
        scrollContent.getChildren().add(makeItemGrid(shop.getBuyItems(), Item.Animal.class, false));
        scrollContent.getChildren().add(makeSectionTitle("Récoltes et Produits animaux"));
        scrollContent.getChildren().add(makeItemGrid(shop.getSellItems(), Item.Crop.class, false));
        scrollContent.getChildren().add(makeItemGrid(shop.getSellItems(), Item.AnimalProduct.class, false));

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(668);
        scrollPane.setStyle("-fx-background: #f5f0e8; -fx-background-color: #f5f0e8; -fx-border-color: transparent;");

        ShopPage.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private Label makeSectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        return lbl;
    }

    private GridPane makeItemGrid(List<Item> source, Class<?> type, boolean isBuy) {
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15); grid.setPadding(new Insets(5));
        int col = 0;
        for (Item item : source) {
            if (type.isInstance(item)) {
                grid.add(createItemCard(item, isBuy), col++, 0);
            }
        }
        return grid;
    }

    private VBox createItemCard(Item item, boolean isBuy) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label emojiLabel = new Label(getEmoji(item));
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(140);

        String labelPrix;
        int prix;
        if (isBuy) {
            labelPrix = "Coût : ";
            prix = item.getBuyPrice();
        } else {
            labelPrix = "Vente : ";
            prix = item.getSellPrice();
        }
        Label priceLabel = new Label(labelPrix + prix + " $");
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #8B6914;");

        Label stockLabel = new Label("Stock : " + player.getInventory().getStockOf(item));
        stockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4a7c2f;");
        if (isBuy) {
            stockLabel.setVisible(false);
        }

        String btnTexte;
        String btnStyle;
        if (isBuy) {
            btnTexte = "Acheter";
            btnStyle = BTN_VERT;
        } else {
            btnTexte = "Vendre";
            btnStyle = BTN_MARRON;
        }
        Button actionBtn = new Button(btnTexte);
        actionBtn.setStyle(btnStyle);

        actionBtn.setOnAction(e -> {
            if (isBuy) {
                boolean success = shop.buyItem(item, 1);
                if (success) {
                    totalDepenses += item.getBuyPrice();
                    actionBtn.setStyle(BTN_VERT);
                    showAlert("Acheté : " + item.getName());
                } else {
                    actionBtn.setStyle(BTN_ROUGE);
                    showAlert("Pas assez d'argent pour : " + item.getName());
                }
            } else {
                boolean success = shop.sellItem(item, 1);
                if (success) {
                    totalRevenues += item.getSellPrice();
                    stockLabel.setText("Stock : " + player.getInventory().getStockOf(item));
                    actionBtn.setStyle(BTN_VERT);
                    showAlert("Vendu : " + item.getName() + " pour " + item.getSellPrice() + " $");
                } else {
                    actionBtn.setStyle(BTN_ROUGE);
                    showAlert("Stock insuffisant pour vendre : " + item.getName());
                }
            }
            updateWalletDisplay();
        });

        card.getChildren().addAll(emojiLabel, nameLabel, descLabel, priceLabel, stockLabel, actionBtn);
        return card;
    }

    // =====================================================================
    // STATISTIQUES - UI
    // =====================================================================
    private void createStatisticUI() {
        StatisticPage.getChildren().clear();
        StatisticPage.setPadding(new Insets(0));
        StatisticPage.setSpacing(0);

        VBox scrollContent = new VBox(25);
        scrollContent.setPadding(new Insets(30));
        scrollContent.setStyle("-fx-background-color: #f5f0e8;");

        Label titre = new Label("Statistiques");
        titre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2d5a1b;");
        scrollContent.getChildren().add(titre);

        scrollContent.getChildren().add(makeSectionTitle("Finances"));
        int beneficeNet = totalRevenues - totalDepenses;
        String couleurBenefice;
        if (beneficeNet >= 0) {
            couleurBenefice = "#4a7c2f";
        } else {
            couleurBenefice = "#cc0000";
        }
        HBox carteFinance = new HBox(20);
        carteFinance.getChildren().addAll(
                makeStatCard("Revenus totaux",   totalRevenues + " $", "#4a7c2f"),
                makeStatCard("Dépenses totales", totalDepenses + " $", "#cc0000"),
                makeStatCard("Bénéfice net",     beneficeNet   + " $", couleurBenefice)
        );
        scrollContent.getChildren().add(carteFinance);

        scrollContent.getChildren().add(makeSectionTitle("Production"));
        HBox carteProduction = new HBox(20);
        carteProduction.getChildren().addAll(
                makeStatCard("Récoltes effectuées", totalRecoltes + " récoltes",              "#8B6914"),
                makeStatCard("Terrains débloqués",  player.getPlotsUnlock().size() + " / 75", "#333333"),
                makeStatCard("Meteo actuelle",      meteo.getDescription(),                   "#4a7c2f")
        );
        scrollContent.getChildren().add(carteProduction);

        scrollContent.getChildren().add(makeSectionTitle("Meilleure culture"));
        VBox meilleurCard = new VBox(10);
        meilleurCard.setPadding(new Insets(20));
        meilleurCard.setAlignment(Pos.CENTER_LEFT);
        meilleurCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label meilleurLabel = new Label(getMeilleurItem());
        meilleurLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #8B6914; -fx-font-weight: bold;");
        meilleurCard.getChildren().add(meilleurLabel);
        scrollContent.getChildren().add(meilleurCard);

        scrollContent.getChildren().add(makeSectionTitle("Inventaire actuel"));
        if (player.getInventory().getItems().isEmpty()) {
            Label vide = new Label("Inventaire vide");
            vide.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
            scrollContent.getChildren().add(vide);
        } else {
            for (Item item : player.getInventory().getItems().values()) {
                HBox ligne = new HBox(10);
                ligne.setAlignment(Pos.CENTER_LEFT);
                ligne.setPadding(new Insets(8, 15, 8, 15));
                ligne.setStyle("-fx-background-color: #ffffff; -fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
                Label emojiLbl  = new Label(getEmoji(item));
                emojiLbl.setStyle("-fx-font-size: 20px;");
                Label nameLbl = new Label(item.getName());
                nameLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-weight: bold;");
                nameLbl.setPrefWidth(200);
                Label qtyLbl = new Label("x" + item.getQuantity());
                qtyLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a7c2f;");
                Label valeurLbl = new Label("Valeur : " + (item.getSellPrice() * item.getQuantity()) + " $");
                valeurLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8B6914;");
                ligne.getChildren().addAll(emojiLbl, nameLbl, qtyLbl, valeurLbl);
                scrollContent.getChildren().add(ligne);
            }
        }

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(668);
        scrollPane.setStyle("-fx-background: #f5f0e8; -fx-background-color: #f5f0e8; -fx-border-color: transparent;");

        StatisticPage.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox makeStatCard(String titre, String valeur, String textColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: " + textColor + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555; -fx-font-weight: bold;");
        titreLabel.setWrapText(true);
        titreLabel.setMaxWidth(180);
        Label valeurLabel = new Label(valeur);
        valeurLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        card.getChildren().addAll(titreLabel, valeurLabel);
        return card;
    }

    private String getMeilleurItem() {
        Item meilleur = null;
        for (Item item : player.getInventory().getItems().values()) {
            if (meilleur == null || item.getSellPrice() > meilleur.getSellPrice()) {
                meilleur = item;
            }
        }
        if (meilleur == null) {
            return "Aucune culture pour le moment";
        }
        return meilleur.getName() + " — " + meilleur.getSellPrice() + " $ / unité";
    }

    // =====================================================================
    // EMOJI
    // =====================================================================
    private String getEmoji(Item item) {
        return switch (item.getName()) {
            case "Blé",            "Blé récolté"      -> "🌾";
            case "Maïs",          "Maïs récolté"     -> "🌽";
            case "Carotte",       "Carotte récoltée" -> "🥕";
            case "Tomate",        "Tomate récoltée"  -> "🍅";
            case "Pomme de terre","PDT récoltée"      -> "🥔";
            case "Vache"   -> "🐄"; case "Lait"     -> "🥛";
            case "Poule"   -> "🐔"; case "Œuf"      -> "🥚";
            case "Cochon"  -> "🐷"; case "Viande"   -> "🥩";
            case "Mouton"  -> "🐑"; case "Laine"    -> "🧶";
            case "Lapin"   -> "🐰"; case "Fourrure" -> "🪶";
            default -> "📦";
        };
    }

    // =====================================================================
    // PLOTS
    // =====================================================================
    private void createPlots() {
        farmGrid.setPrefWidth(995);
        farmGrid.setPrefHeight(668);
        farmGrid.setHgap(4);
        farmGrid.setVgap(4);
        farmGrid.setStyle("-fx-background-color: #8B6914; -fx-padding: 4;");

        farmGrid.getColumnConstraints().clear();
        for (int c = 0; c < 5; c++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            farmGrid.getColumnConstraints().add(col);
        }

        farmGrid.getRowConstraints().clear();
        for (int r = 0; r < 15; r++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            farmGrid.getRowConstraints().add(row);
        }

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 5; j++) {
                plots[i][j] = new Plot(true);

                StackPane stack = new StackPane();
                stack.setMaxWidth(Double.MAX_VALUE);
                stack.setMaxHeight(Double.MAX_VALUE);

                Button terrainButton = new Button();
                terrainButton.setId("terrain_" + i + "_" + j);
                terrainButton.setMaxWidth(Double.MAX_VALUE);
                terrainButton.setMaxHeight(Double.MAX_VALUE);
                terrainButton.setStyle(PLOT_VIDE);

                Button feedButton = new Button("🍽");
                feedButton.setId("feed_" + i + "_" + j);
                feedButton.setVisible(false);
                feedButton.setStyle(FEED_NORMAL);
                feedButton.setMaxWidth(30);
                feedButton.setMaxHeight(20);
                StackPane.setAlignment(feedButton, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(feedButton, new Insets(0, 4, 4, 0));

                stack.getChildren().addAll(terrainButton, feedButton);
                farmGrid.add(stack, j, i);

                final int row = i, col = j;
                terrainButton.setOnAction(e -> onPlotClicked(row, col));
            }
        }
    }

    private void onPlotClicked(int row, int col) {
        Plot plot = plots[row][col];

        if (plot.isLocked())           { unlockPlot(row, col); return; }
        if (plot.isReady())            { harvestPlot(row, col); return; }
        if (plot.getContenu() != null) { showAlert("Ce plot est déjà occupé !"); return; }
        if (selectedItem == null)      { showAlert("Aucun item sélectionné !"); return; }

        Button btn     = (Button) farmGrid.lookup("#terrain_" + row + "_" + col);
        Button feedBtn = (Button) farmGrid.lookup("#feed_"    + row + "_" + col);

        if (selectedItem instanceof Item.Seed) {
            Item.Seed seed = (Item.Seed) selectedItem;
            if (player.getInventory().getStockOf(seed) <= 0) {
                showAlert("Plus de graines : " + seed.getName()); return;
            }
            if (plot.ajouterContenu(seed)) {
                player.getInventory().removeItem(seed, 1);
                btn.setText(getEmoji(seed));
                btn.setStyle(PLOT_LIBRE);
                int tempsModifie = meteo.appliquerMeteo(seed.getGrowthTime());
                plot.startGrowth(seed, tempsModifie, () -> {
                    btn.setStyle(PLOT_PRET);
                    showAlert("Récolte prête en [" + row + "," + col + "] !");
                });
                updateWalletDisplay();
            }

        } else if (selectedItem instanceof Item.Animal) {
            Item.Animal animal = (Item.Animal) selectedItem;
            if (player.getInventory().getStockOf(animal) <= 0) {
                showAlert("Plus d'animaux : " + animal.getName()); return;
            }
            if (plot.ajouterContenu(animal)) {
                player.getInventory().removeItem(animal, 1);
                btn.setText(getEmoji(animal));
                btn.setStyle(PLOT_ANIMAL);
                feedBtn.setVisible(true);
                wireFeedButton(feedBtn, btn, animal, plot, row, col);
                updateWalletDisplay();
            }

        } else {
            showAlert("Cet item ne peut pas être placé sur un terrain !");
        }
    }

    private void wireFeedButton(Button feedBtn, Button btn, Item.Animal animal, Plot plot, int row, int col) {
        feedBtn.setOnAction(e -> {
            boolean fed = animal.feed(plot, player.getInventory(), () -> {
                btn.setStyle(PLOT_PRET);
                showAlert("Produit prêt en [" + row + "," + col + "] !");
            });
            if (fed) {
                feedBtn.setVisible(false);
                showAlert(animal.getName() + " nourri !");
            } else {
                feedBtn.setStyle(FEED_ROUGE);
                showAlert("Pas de " + animal.getFoodRequired() + " pour nourrir " + animal.getName() + " !");
            }
            createInventoryUI();
            e.consume();
        });
    }

    private void harvestPlot(int row, int col) {
        Plot   plot    = plots[row][col];
        Item   contenu = plot.getContenu();
        Button btn     = (Button) farmGrid.lookup("#terrain_" + row + "_" + col);

        if (contenu instanceof Item.Seed) {
            int rendement     = plot.getRendement();
            Item.Crop recolte = plot.harvestSeed();
            if (recolte != null) {
                player.getInventory().addItem(recolte, rendement);
                totalRecoltes++;
                btn.setStyle(PLOT_LIBRE);
                btn.setText("");
                showAlert("Récolté " + rendement + "x " + recolte.getName() + " !");
            }

        } else if (contenu instanceof Item.Animal) {
            Item.Animal        animal  = (Item.Animal) contenu;
            Item.AnimalProduct produit = plot.harvestAnimal();
            if (produit != null) {
                player.getInventory().addItem(produit, 1);
                totalRecoltes++;
                btn.setStyle(PLOT_ANIMAL);
                Button feedBtn = (Button) farmGrid.lookup("#feed_" + row + "_" + col);
                feedBtn.setVisible(true);
                feedBtn.setStyle(FEED_NORMAL);
                wireFeedButton(feedBtn, btn, animal, plot, row, col);
                showAlert(produit.getName() + " récolté !");
            }
        }

        updateWalletDisplay();
    }

    private void unlockPlot(int row, int col) {
        Plot    plot    = plots[row][col];
        boolean success = player.unlock(plot);
        if (success) {
            Button btn = (Button) farmGrid.lookup("#terrain_" + row + "_" + col);
            btn.setStyle(PLOT_LIBRE);
            showAlert("Terrain débloqué pour " + plot.getValue() + " $");
        } else {
            showAlert("Pas assez d'argent ! Coût : " + plot.getValue() + " $");
        }
        updateWalletDisplay();
    }

    // =====================================================================
    // SAVE / LOAD
    // =====================================================================
    @FXML
    public void saveGame() {
        SaveData.save(player, plots, totalRevenues, totalDepenses, totalRecoltes);
        showAlert("Partie sauvegardée !");
    }

    public void loadGame() {
        SaveData data = SaveData.load();
        if (data == null) return;

        player.getWallet().setDollars(data.dollars);
        totalRevenues = data.totalRevenues;
        totalDepenses = data.totalDepenses;
        totalRecoltes = data.totalRecoltes;

        player.getInventory().clear();
        for (SaveData.ItemData id : data.inventory) {
            Item item = shop.findByName(id.name);
            if (item != null) {
                player.getInventory().addItem(item, id.quantity);
                item.setSelected(id.selected);
                if (id.selected) selectedItem = item;
            }
        }

        for (SaveData.PlotData pd : data.plots) {
            Plot   plot = plots[pd.row][pd.col];
            Button btn  = (Button) farmGrid.lookup("#terrain_" + pd.row + "_" + pd.col);
            plot.setLocked(pd.locked);
            plot.setReady(pd.ready);
            plot.setFed(pd.fed);

            if (pd.contenuName != null) {
                Item contenu = shop.findByName(pd.contenuName);
                if (contenu != null) {
                    plot.ajouterContenu(contenu);
                    btn.setText(getEmoji(contenu));

                    if (pd.ready) {
                        btn.setStyle(PLOT_PRET);
                    } else if ("Animal".equals(pd.contenuType)) {
                        btn.setStyle(PLOT_ANIMAL);
                        Button feedBtn = (Button) farmGrid.lookup("#feed_" + pd.row + "_" + pd.col);
                        feedBtn.setVisible(true);
                        if (!pd.fed) {
                            Item.Animal animal = (Item.Animal) contenu;
                            wireFeedButton(feedBtn, btn, animal, plot, pd.row, pd.col);
                        }
                    } else {
                        btn.setStyle(PLOT_LIBRE);
                    }
                }
            } else if (!pd.locked) {
                btn.setStyle(PLOT_LIBRE);
                btn.setText("");
            }
        }

        updateWalletDisplay();
        showAlert("Partie chargée !");
    }

} // fin Controller