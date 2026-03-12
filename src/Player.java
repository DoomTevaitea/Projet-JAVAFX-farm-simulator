import java.util.ArrayList;

public class Player {

    protected String           nom;
    protected Inventory        inventory   = new Inventory();
    protected Wallet           wallet      = new Wallet();
    protected ArrayList<Plot>  plotsUnlock = new ArrayList<>();

    public Player(String nom) {
        this.nom = nom;
    }


    // DEBLOQUER UN PLOT
    public boolean unlock(Plot plot) {
        if (!plot.isLocked()) {
            System.out.println("Ce plot est déjà débloqué !");
            return true;
        }

        if (wallet.canAfford(plot.getValue())) {
            wallet.removeDollars(plot.getValue());
            plot.setLocked(false);
            plotsUnlock.add(plot);
            System.out.println("Plot débloqué ! Solde restant : " + wallet);
            return true;
        }

        System.out.println("Pas assez d'argent ! Solde : " + wallet + " / Coût : " + plot.getValue() + " $");
        return false;
    }


    // RESET — nouvelle partie
    public void reset() {
        inventory.clear();
        wallet.setDollars(500);
        plotsUnlock.clear();
        System.out.println("Joueur réinitialisé.");
    }

    public String          getNom()         { return nom; }
    public Inventory       getInventory()   { return inventory; }
    public Wallet          getWallet()      { return wallet; }
    public ArrayList<Plot> getPlotsUnlock() { return plotsUnlock; }

    @Override
    public String toString() {
        return "Player{nom='" + nom + "', solde=" + wallet + ", plots débloqués=" + plotsUnlock.size() + "}";
    }
}