import java.util.ArrayList;

public class Farm {

    private int             nbTerrain;
    private Shop            shop;
    private ArrayList<Plot> plots = new ArrayList<>();

    public Farm(int nbTerrain, Shop shop) {
        this.nbTerrain = nbTerrain;
        this.shop      = shop;
        initPlots();
    }

    private void initPlots() {
        for (int i = 0; i < nbTerrain; i++) {
            plots.add(new Plot(true));
        }
        System.out.println("Farm initialisée avec " + nbTerrain + " plots.");
    }

    // TROUVER UN PLOT LIBRE
    public Plot getFirstFreePlot() {
        for (Plot plot : plots) {
            if (!plot.isLocked() && plot.getContenu() == null) {
                return plot;
            }
        }
        return null;
    }

    // COMPTER
    public int countUnlocked() {
        int count = 0;
        for (Plot plot : plots) {
            if (!plot.isLocked()) count++;
        }
        return count;
    }

    public int countReady() {
        int count = 0;
        for (Plot plot : plots) {
            if (plot.isReady()) count++;
        }
        return count;
    }

    public int countAnimals() {
        int count = 0;
        for (Plot plot : plots) {
            if (plot.getContenu() instanceof Item.Animal) count++;
        }
        return count;
    }

    public int countCrops() {
        int count = 0;
        for (Plot plot : plots) {
            if (plot.getContenu() instanceof Item.Seed) count++;
        }
        return count;
    }


    public int             getNbTerrain() { return nbTerrain; }
    public Shop            getShop()      { return shop; }
    public ArrayList<Plot> getPlots()     { return plots; }
}