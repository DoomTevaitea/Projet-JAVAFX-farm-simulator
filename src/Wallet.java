public class Wallet {

    protected int dollars;

    public Wallet() {
        this.dollars = 500; // ✅ montant de départ
    }

    public int getDollars() {
        return dollars;
    }

    public void setDollars(int amount) {
        this.dollars = amount;
    }

    public void addDollars(int amount) {
        if (amount > 0) { // ✅ on vérifie que le montant est positif
            this.dollars += amount;
        }
    }

    public void removeDollars(int amount) {
        if (amount > 0 && dollars >= amount) { // ✅ double vérification
            this.dollars -= amount;
        }
    }

    public boolean canAfford(int amount) { // ✅ méthode utilitaire pratique
        return dollars >= amount;
    }

    @Override
    public String toString() {
        return dollars + " $";
    }
}