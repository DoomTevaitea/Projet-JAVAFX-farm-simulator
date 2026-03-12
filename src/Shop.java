import java.util.ArrayList;
import java.util.List;

public class Shop {

    private Inventory inventory;
    private Wallet    wallet;

    // CATALOGUE — items disponibles
    private List<Item> buyItems  = new ArrayList<>();
    private List<Item> sellItems = new ArrayList<>();

    public Shop(Inventory inventory, Wallet wallet) {
        this.inventory = inventory;
        this.wallet    = wallet;
        initItems();
    }

    private void initItems() {

        // Graines
        buyItems.add(new Item.Seed("Blé",            "Graine de blé, pousse vite",           5,   8, 1, 3,  2, "Blé récolté"));
        buyItems.add(new Item.Seed("Maïs",           "Graine de maïs, rendement moyen",     10,  18, 1, 4,  3, "Maïs récolté"));
        buyItems.add(new Item.Seed("Carotte",        "Graine de carotte, pousse vite",        8,  14, 1, 2,  2, "Carotte récoltée"));
        buyItems.add(new Item.Seed("Tomate",         "Graine de tomate, longue pousse",      18,  40, 1, 5,  4, "Tomate récoltée"));
        buyItems.add(new Item.Seed("Pomme de terre", "Graine de PDT, bon rendement",         12,  22, 1, 3,  3, "PDT récoltée"));

        // Animaux
        buyItems.add(new Item.Animal("Lapin",  "Produit de la fourrure. Nourrir : Carotte récoltée",  80,  30, 1,  8,  8, "Fourrure", "Carotte récoltée"));
        buyItems.add(new Item.Animal("Poule",  "Produit des œufs. Nourrir : Maïs récolté",           120,  50, 1, 10, 10, "Œuf",      "Maïs récolté"));
        buyItems.add(new Item.Animal("Mouton", "Produit de la laine. Nourrir : Blé récolté",          200,  80, 1, 20, 15, "Laine",    "Blé récolté"));
        buyItems.add(new Item.Animal("Cochon", "Produit de la viande. Nourrir : PDT récoltée",        280, 100, 1, 25, 20, "Viande",   "PDT récoltée"));
        buyItems.add(new Item.Animal("Vache",  "Produit du lait. Nourrir : Blé récolté",             400, 150, 1, 30, 30, "Lait",     "Blé récolté"));

        // Récoltes (vente uniquement)
        sellItems.add(new Item.Crop("Blé récolté",      "Blé prêt à vendre",      0,  12, 0, 0));
        sellItems.add(new Item.Crop("Maïs récolté",     "Maïs prêt à vendre",     0,  22, 0, 0));
        sellItems.add(new Item.Crop("Carotte récoltée", "Carotte prête à vendre", 0,  16, 0, 0));
        sellItems.add(new Item.Crop("Tomate récoltée",  "Tomate prête à vendre",  0,  45, 0, 0));
        sellItems.add(new Item.Crop("PDT récoltée",     "Pomme de terre prête",   0,  28, 0, 0));

        // Produits animaux (vente uniquement)
        sellItems.add(new Item.AnimalProduct("Fourrure", "Produit par le lapin",  0,  20, 0));
        sellItems.add(new Item.AnimalProduct("Œuf",      "Produit par la poule",  0,  30, 0));
        sellItems.add(new Item.AnimalProduct("Laine",    "Produit par le mouton", 0,  55, 0));
        sellItems.add(new Item.AnimalProduct("Viande",   "Produit par le cochon", 0,  80, 0));
        sellItems.add(new Item.AnimalProduct("Lait",     "Produit par la vache",  0, 120, 0));
    }

    // ACHAT / VENTE
    public boolean buyItem(Item item, int quantity) {
        int totalPrice = item.getBuyPrice() * quantity;

        if (wallet.canAfford(totalPrice)) {
            wallet.removeDollars(totalPrice);
            inventory.addItem(item, quantity);
            System.out.println("Acheté : " + quantity + "x " + item.getName());
            return true;
        }

        System.out.println("Pas assez d'argent pour : " + item.getName());
        return false;
    }

    public boolean sellItem(Item item, int quantity) {
        if (inventory.hasItem(item, quantity)) {
            inventory.removeItem(item, quantity);
            wallet.addDollars(item.getSellPrice() * quantity);
            System.out.println("Vendu : " + quantity + "x " + item.getName() + " pour " + item.getSellPrice() * quantity + " $");
            return true;
        }

        System.out.println("Stock insuffisant pour : " + item.getName());
        return false;
    }

    // ✅ Cherche un item dans tout le catalogue par son nom
    public Item findByName(String name) {
        for (Item item : buyItems)  { if (item.getName().equals(name)) return item; }
        for (Item item : sellItems) { if (item.getName().equals(name)) return item; }
        return null;
    }

    public List<Item> getBuyItems()  { return buyItems; }
    public List<Item> getSellItems() { return sellItems; }
}