import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Random;

public class Meteo {

    private String condition;
    private double multiplicateur;
    private Random random = new Random();
    private Runnable onChangement;

    public Meteo() {
        tirerMeteo();
    }

    private void tirerMeteo() {
        int tirage = random.nextInt(100);

        if (tirage < 10) {
            condition      = "Soleil";
            multiplicateur = 0.5;
        } else if (tirage < 30) {
            condition      = "Pluvieux";
            multiplicateur = 1.5;
        } else {
            condition      = "Nuageux";
            multiplicateur = 1.0;
        }
    }


    public void demarrerCycle(Runnable onChangementCallback) {
        this.onChangement = onChangementCallback;

        Timeline cycle = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    tirerMeteo();
                    if (onChangement != null) onChangement.run();
                })
        );
        cycle.setCycleCount(Timeline.INDEFINITE);
        cycle.play();
    }


    public int appliquerMeteo(int tempsBase) {
        double resultat = tempsBase * multiplicateur;
        return (int) Math.max(1, resultat);
    }


    public String getCondition()      { return condition; }
    public double getMultiplicateur() { return multiplicateur; }

    public String getDescription() {
        return condition + " (x" + multiplicateur + ")";
    }
}