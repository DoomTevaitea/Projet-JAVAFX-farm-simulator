import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Plot {

    private boolean locked;
    private Item    contenu;
    private int     vie;
    private int     tempsDeVie;
    private boolean operationnel;
    private int     niveauAmelioration;
    private int     value;
    private boolean ready;
    private boolean fed;

    // Callbacks vers le Controller pour mettre à jour l'UI
    private Runnable onReady;   // appelé quand la croissance/production est finie
    private Runnable onHarvest; // appelé après la récolte

    public Plot(boolean locked) {
        this.locked             = locked;
        this.vie                = 100;
        this.tempsDeVie         = 0;
        this.operationnel       = true;
        this.niveauAmelioration = 0;
        this.value              = 100;
        this.ready              = false;
        this.fed                = false;
    }

    // CONTENU
    public boolean ajouterContenu(Item item) {
        if (locked || !operationnel) return false;
        if (contenu == null) { contenu = item; return true; }
        return false;
    }

    public void retirerContenu() { contenu = null; }

    // CROISSANCE — Graine
    public void startGrowth(Item.Seed seed, int tempsModifie, Runnable onReadyCallback) {
        this.onReady = onReadyCallback;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(tempsModifie), e -> {
                    this.ready = true;
                    if (onReady != null) onReady.run();
                })
        );
        timeline.setCycleCount(1);
        timeline.play();
    }

    public void startProduction(Item.Animal animal, Runnable onReadyCallback) {
        this.onReady = onReadyCallback;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(animal.getProductionTime()), e -> {
                    this.ready = true;
                    if (onReady != null) onReady.run();
                })
        );
        timeline.setCycleCount(1);
        timeline.play();
    }

    // RÉCOLTE

    // Récolte une graine → retourne le Crop produit (null si pas de graine)
    public Item.Crop harvestSeed() {
        if (!(contenu instanceof Item.Seed) || !ready) return null;

        Item.Seed seed = (Item.Seed) contenu;

        Item.Crop recolte = new Item.Crop(
                seed.getCropResult(),
                seed.getCropResult() + " prêt à vendre",
                0,
                seed.getSellPrice(),
                0,
                0
        );

        retirerContenu();
        this.ready = false;

        return recolte;
    }

    // Récolte un animal → retourne l'AnimalProduct produit (null si pas d'animal)
    public Item.AnimalProduct harvestAnimal() {
        if (!(contenu instanceof Item.Animal) || !ready) return null;

        Item.Animal animal = (Item.Animal) contenu;

        Item.AnimalProduct produit = new Item.AnimalProduct(
                animal.getProductName(),
                "Produit par " + animal.getName(),
                0,
                15,
                0
        );

        this.ready = false;
        this.fed   = false;

        return produit;
    }

    // rendement (pour l'inventaire)
    public int getRendement() {
        if (contenu instanceof Item.Seed) {
            return ((Item.Seed) contenu).getRendement();
        }
        return 1;
    }

    // AMELIORATION / REPARATION
    public void ameliorer() { niveauAmelioration++; vie += 20; }
    public void reparer()   { operationnel = true; }


    public void setLocked(boolean locked) { this.locked = locked; }
    public void setReady(boolean ready)   { this.ready = ready; }
    public void setFed(boolean fed)       { this.fed = fed; }


    public boolean isLocked()              { return locked; }
    public boolean isReady()               { return ready; }
    public boolean isFed()                 { return fed; }
    public boolean isOperationnel()        { return operationnel; }
    public Item    getContenu()            { return contenu; }
    public int     getVie()                { return vie; }
    public int     getTempsDeVie()         { return tempsDeVie; }
    public int     getNiveauAmelioration() { return niveauAmelioration; }
    public int     getValue()              { return value; }
}