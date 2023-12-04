package hospital.simulator.monitors;

import hospital.simulator.models.Consultorio;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;

public class NurseMonitor {
    private List<ImageView> availableNurses;

    public NurseMonitor(List<ImageView> nurses) {
        this.availableNurses = new LinkedList<>(nurses);
    }

    public synchronized void assignNurseToConsultorio(Consultorio consultorio, Runnable onFinished) {
        if (!availableNurses.isEmpty()) {
            ImageView nurse = availableNurses.remove(0);
            Platform.runLater(() -> {
                animateNurseToConsultorio(nurse, consultorio, () -> {
                    availableNurses.add(nurse);
                    animateNurseBack(nurse);
                    onFinished.run();
                });
            });
        }
    }

    private void animateNurseToConsultorio(ImageView nurse, Consultorio consultorio, Runnable onFinished) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), nurse);
        toConsultorio.setToX(consultorio.getVisualRepresentation().getLayoutX() - nurse.getLayoutX());
        toConsultorio.setToY(consultorio.getVisualRepresentation().getLayoutY() - nurse.getLayoutY());

        toConsultorio.setOnFinished(event -> {
            new Thread(() -> {
                Platform.runLater(() -> {
                    onFinished.run();
                    animateNurseBack(nurse);
                });
            }).start();
        });
        toConsultorio.play();
    }

    private void animateNurseBack(ImageView nurse) {
        TranslateTransition backToPlace = new TranslateTransition(Duration.seconds(3), nurse);
        backToPlace.setToX(0);
        backToPlace.setToY(0);
        backToPlace.play();
    }
}
