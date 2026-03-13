public class Wallet {

    protected int dollars;

    public Wallet() {
        this.dollars = 500;
    }

    public int getDollars() {
        return dollars;
    }

    public void setDollars(int amount) {
        this.dollars = amount;
    }

    public void addDollars(int amount) {
        if (amount > 0) {
            this.dollars += amount;
        }
    }

    public void removeDollars(int amount) {
        if (amount > 0 && dollars >= amount) { 
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