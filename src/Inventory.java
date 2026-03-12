import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private Map<String, Item> items = new HashMap<>();

    public void addItem(Item item, int quantity) {
        if (items.containsKey(item.getName())) {
            items.get(item.getName()).addQuantity(quantity);
        } else {
            item.addQuantity(quantity);
            items.put(item.getName(), item);
        }
        System.out.println("Ajouté : " + quantity + "x " + item.getName());
    }

    public void removeItem(Item item, int quantity) {
        if (!items.containsKey(item.getName())) return;

        Item existing = items.get(item.getName());
        existing.removeQuantity(quantity);

        if (existing.getQuantity() <= 0) {
            items.remove(item.getName());
        }

        System.out.println("Retiré : " + quantity + "x " + item.getName());
    }


    // VERIFICATIONS
    public boolean hasItem(Item item, int quantity) {
        if (!items.containsKey(item.getName())) return false;
        return items.get(item.getName()).getQuantity() >= quantity;
    }

    // Retourne la quantité d'un item par son nom (0 si absent)
    public int getStockOf(String name) {
        Item item = items.get(name);
        return item != null ? item.getQuantity() : 0;
    }

    // Retourne la quantité d'un item par référence (0 si absent)
    public int getStockOf(Item item) {
        return getStockOf(item.getName());
    }


    public void clear() {
        items.clear();
        System.out.println("Inventaire vidé.");
    }


    public Map<String, Item> getItems() {
        return items;
    }
}