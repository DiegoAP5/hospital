package hospital.simulator.threads;

import hospital.simulator.models.Consultorio;
import hospital.simulator.monitors.DoctorPatientMonitor;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

public class PatientCreationThread implements Runnable {
    private final Pane mainLayout;
    private final Rectangle receptionist;
    private final DoctorPatientMonitor monitor;
    private final List<Consultorio> consultorios;
    private final List<ImageView> doctors;

    public PatientCreationThread(Pane mainLayout, Rectangle receptionist, DoctorPatientMonitor monitor, List<Consultorio> consultorios, List<ImageView> doctors) {
        this.mainLayout = mainLayout;
        this.receptionist = receptionist;
        this.monitor = monitor;
        this.consultorios = consultorios;
        this.doctors = doctors;
    }

    @Override
    public void run() {
        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(2000);
                Platform.runLater(this::createAndAnimatePatient);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void createAndAnimatePatient() {
        ImageView patient = new ImageView("file:src/main/resources/assets/images/paciente.png");
        patient.setLayoutX(10);
        patient.setLayoutY(10);
        patient.setFitHeight(30);
        patient.setFitWidth(30);
        mainLayout.getChildren().add(patient);

        animatePatientToReception(patient);
    }

    private void animatePatientToReception(ImageView patient) {
        TranslateTransition toReception = new TranslateTransition(Duration.seconds(3), patient);
        toReception.setToX(receptionist.getLayoutX() - patient.getLayoutX());
        toReception.setToY(receptionist.getLayoutY() - patient.getLayoutY());

        toReception.setOnFinished(event -> monitor.assignDoctorToPatient(patient, consultorios));
        toReception.play();
    }
}
