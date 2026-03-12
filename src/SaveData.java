import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SaveData {

    // CHAMPS SAUVEGARDÉS
    public int dollars;
    public int totalRevenues;
    public int totalDepenses;
    public int totalRecoltes;

    public List<ItemData>  inventory = new ArrayList<>();
    public List<PlotData>  plots     = new ArrayList<>();

    // INNER CLASSES
    public static class ItemData {
        public String  type;
        public String  name;
        public int     quantity;
        public boolean selected;
    }

    public static class PlotData {
        public int     row;
        public int     col;
        public boolean locked;
        public boolean ready;
        public boolean fed;
        public String  contenuType;
        public String  contenuName;
    }

    // SAVE
    public static void save(Player player, Plot[][] plots,
                            int totalRevenues, int totalDepenses, int totalRecoltes) {
        SaveData data = new SaveData();

        // Wallet + stats
        data.dollars       = player.getWallet().getDollars();
        data.totalRevenues = totalRevenues;
        data.totalDepenses = totalDepenses;
        data.totalRecoltes = totalRecoltes;

        // Inventaire
        for (Item item : player.getInventory().getItems().values()) {
            ItemData id  = new ItemData();
            id.name      = item.getName();
            id.quantity  = item.getQuantity();
            id.selected  = item.isSelected();

            if      (item instanceof Item.Seed)          id.type = "Seed";
            else if (item instanceof Item.Animal)        id.type = "Animal";
            else if (item instanceof Item.Crop)          id.type = "Crop";
            else if (item instanceof Item.AnimalProduct) id.type = "AnimalProduct";

            data.inventory.add(id);
        }

        // Plots
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 5; j++) {
                Plot    plot = plots[i][j];
                PlotData pd  = new PlotData();
                pd.row    = i;
                pd.col    = j;
                pd.locked = plot.isLocked();
                pd.ready  = plot.isReady();
                pd.fed    = plot.isFed();

                Item contenu = plot.getContenu();
                if (contenu != null) {
                    pd.contenuName = contenu.getName();
                    if      (contenu instanceof Item.Seed)   pd.contenuType = "Seed";
                    else if (contenu instanceof Item.Animal) pd.contenuType = "Animal";
                }

                data.plots.add(pd);
            }
        }

        // Écriture JSON
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(Path.of("save.json"), gson.toJson(data));
            System.out.println("Sauvegarde réussie !");
        } catch (IOException e) {
            System.out.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    // =====================================================================
    // LOAD
    // =====================================================================
    public static SaveData load() {
        Path savePath = Path.of("save.json");

        if (!Files.exists(savePath)) {
            System.out.println("Aucune sauvegarde trouvée.");
            return null;
        }

        try {
            String   json = Files.readString(savePath);
            SaveData data = new Gson().fromJson(json, SaveData.class);

            if (data == null) {
                System.out.println("⚠️ Sauvegarde corrompue ou vide.");
                return null;
            }

            System.out.println("Sauvegarde chargée !");
            return data;

        } catch (IOException e) {
            System.out.println("Erreur chargement : " + e.getMessage());
            return null;
        }
    }
}