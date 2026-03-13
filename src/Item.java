import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public abstract class Item {

    protected String  name;
    protected String  description;
    protected int     buyPrice;
    protected int     sellPrice;
    protected int     quantity;
    protected boolean selected;

    public Item(String name, String description, int buyPrice, int sellPrice, int quantity) {
        this.name        = name;
        this.description = description;
        this.buyPrice    = buyPrice;
        this.sellPrice   = sellPrice;
        this.quantity    = quantity;
        this.selected    = false;
    }

    public String  getName()        { return name; }
    public String  getDescription() { return description; }
    public int     getBuyPrice()    { return buyPrice; }
    public int     getSellPrice()   { return sellPrice; }
    public int     getQuantity()    { return quantity; }
    public boolean isSelected()     { return selected; }


    public void setSelected(boolean selected) { this.selected = selected; }


    public void addQuantity(int amount) {
        if (amount > 0) this.quantity += amount;
    }

    public void removeQuantity(int amount) {
        if (amount > 0 && quantity >= amount) this.quantity -= amount;
    }


    public static class Seed extends Item {

        private int    growthTime;
        private int    rendement;
        private String cropResult;

        public Seed(String name, String description, int buyPrice, int sellPrice, int quantity,
                    int growthTime, int rendement, String cropResult) {
            super(name, description, buyPrice, sellPrice, quantity);
            this.growthTime = growthTime;
            this.rendement  = rendement;
            this.cropResult = cropResult;
        }

        public int    getGrowthTime() { return growthTime; }
        public int    getRendement()  { return rendement; }
        public String getCropResult() { return cropResult; }


        public Crop createCrop() {
            return new Crop(
                    cropResult,
                    cropResult + " prêt à vendre",
                    0,
                    sellPrice,
                    0,
                    0
            );
        }
    }

    public static class Crop extends Item {

        private int nutritionValue;

        public Crop(String name, String description, int buyPrice, int sellPrice,
                    int quantity, int nutritionValue) {
            super(name, description, buyPrice, sellPrice, quantity);
            this.nutritionValue = nutritionValue;
        }

        public int getNutritionValue() { return nutritionValue; }
    }


    public static class Animal extends Item {

        private int    hunger;
        private int    productionTime;
        private String productName;
        private String foodRequired;

        public Animal(String name, String description, int buyPrice, int sellPrice, int quantity,
                      int hunger, int productionTime, String productName, String foodRequired) {
            super(name, description, buyPrice, sellPrice, quantity);
            this.hunger         = hunger;
            this.productionTime = productionTime;
            this.productName    = productName;
            this.foodRequired   = foodRequired;
        }

        public int    getHunger()         { return hunger; }
        public int    getProductionTime() { return productionTime; }
        public String getProductName()    { return productName; }
        public String getFoodRequired()   { return foodRequired; }


        public boolean canBeFed(Plot plot) {
            return plot.getContenu() == this
                    && !plot.isFed()
                    && !plot.isReady();
        }


        public AnimalProduct createProduct() {
            return new AnimalProduct(
                    productName,
                    "Produit par " + name,
                    0,
                    15,
                    0
            );
        }


        public boolean feed(Plot plot, Inventory inventory, Runnable onReadyCallback) {
            // Vérifie que l'animal peut être nourri
            if (!canBeFed(plot)) {
                System.out.println(name + " est déjà nourri ou en production !");
                return false;
            }


            Item food = inventory.getItems().get(foodRequired);
            if (food == null || food.getQuantity() <= 0) {
                System.out.println("Pas de " + foodRequired + " en inventaire !");
                return false;
            }


            inventory.removeItem(food, 1);
            plot.setFed(true);
            plot.startProduction(this, onReadyCallback);

            System.out.println(name + " nourri avec " + foodRequired + " ! Production lancée.");
            return true;
        }
    }

    public static class AnimalProduct extends Item {

        public AnimalProduct(String name, String description, int buyPrice,
                             int sellPrice, int quantity) {
            super(name, description, buyPrice, sellPrice, quantity);
        }
    }

    public static class Food extends Item {

        private int nutrition;

        public Food(String name, String description, int buyPrice, int sellPrice,
                    int quantity, int nutrition) {
            super(name, description, buyPrice, sellPrice, quantity);
            this.nutrition = nutrition;
        }

        public int getNutrition() { return nutrition; }
    }
}