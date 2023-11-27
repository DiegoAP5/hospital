package hospital.simulator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class simulator extends Application {
    @Override
    public void start(Stage primaryStage) {
        Pane mainLayout = new Pane();
        Rectangle receptionist = createReceptionist();
        mainLayout.getChildren().add(receptionist);

        List<Consultorio> consultorios = createConsultationRooms();
        consultorios.forEach(c -> mainLayout.getChildren().add(c.getVisualRepresentation()));

        Pane doctorArea = createDoctorArea();
        mainLayout.getChildren().add(doctorArea);

        Pane nurseArea = createNurseArea();
        mainLayout.getChildren().add(nurseArea);

        PatientMonitor monitor = new PatientMonitor(10);
        schedulePatientCreation(monitor, mainLayout, receptionist, consultorios);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setTitle("Hospital Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Pane createNurseArea() {
        Pane nurseArea = new Pane();
        nurseArea.setLayoutX(750); // A la derecha de la ventana
        nurseArea.setLayoutY(200);

        for (int i = 0; i < 5; i++) {
            Rectangle nurse = new Rectangle(15, 15, Color.GREEN);
            nurse.setLayoutX(0);
            nurse.setLayoutY(15 * i);
            nurseArea.getChildren().add(nurse);
        }

        return nurseArea;
    }

    private Rectangle createReceptionist() {
        Rectangle receptionist = new Rectangle(50, 20, Color.YELLOW); // Rectángulo más ancho
        receptionist.setLayoutX(10); // Posición de la recepcionista
        receptionist.setLayoutY(200);
        return receptionist;
    }

    private Pane createDoctorArea() {
        Pane doctorArea = new Pane();
        doctorArea.setLayoutX(300); // Posicionamiento en el centro inferior
        doctorArea.setLayoutY(500);

        for (int i = 0; i < 15; i++) {
            Rectangle doctor = new Rectangle(15, 15, Color.RED);
            doctor.setLayoutX(15 * i);
            doctor.setLayoutY(0);
            doctorArea.getChildren().add(doctor);
        }

        return doctorArea;
    }

    public class PatientMonitor {
        private final int totalPatients;
        private int currentPatient = 0;

        public PatientMonitor(int totalPatients) {
            this.totalPatients = totalPatients;
        }

        public synchronized void createAndAnimateNextPatient(Pane mainLayout, Rectangle receptionist, List<Consultorio> consultorios) {
            if (currentPatient < totalPatients) {
                createAndAnimatePatient(currentPatient, mainLayout, receptionist, consultorios);
                currentPatient++;
            }
        }
    }

    private List<Rectangle> createPatients(int numberOfPatients) {
        List<Rectangle> patients = new ArrayList<>();
        for (int i = 0; i < numberOfPatients; i++) {
            Rectangle patient = new Rectangle(20, 20, Color.BLUE);
            patient.setLayoutX(10); // Posición inicial X (esquina superior izquierda)
            patient.setLayoutY(10 + 30 * i); // Posición inicial Y, ligeramente desplazados
            patients.add(patient);
        }
        return patients;
    }

    class Consultorio {
        Rectangle visualRepresentation;
        boolean isOccupied;

        Consultorio(int x, int y) {
            visualRepresentation = new Rectangle(30, 30);
            visualRepresentation.setLayoutX(x);
            visualRepresentation.setLayoutY(y);
            isOccupied = false;
        }

        Rectangle getVisualRepresentation() {
            return visualRepresentation;
        }

        boolean isOccupied() {
            return isOccupied;
        }

        void setOccupied(boolean occupied) {
            isOccupied = occupied;
        }
    }

    private List<Consultorio> createConsultationRooms() {
        List<Consultorio> consultationRooms = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Consultorio consultorio = new Consultorio(50 + (i % 10) * 60, 30 + (i / 10) * 60); // Ajuste de posicionamiento
            consultationRooms.add(consultorio);
        }
        return consultationRooms;
    }

    private void animatePatientToReception(Rectangle patient, Rectangle receptionist, List<Consultorio> consultorios, int delay) {
        TranslateTransition toReception = new TranslateTransition(Duration.seconds(3), patient);
        toReception.setDelay(Duration.seconds(delay));
        toReception.setToX(receptionist.getLayoutX() - patient.getLayoutX());
        toReception.setToY(receptionist.getLayoutY() - patient.getLayoutY());

        toReception.setOnFinished(event -> assignConsultorio(patient, consultorios));
        toReception.play();
    }

    private void assignConsultorio(Rectangle patient, List<Consultorio> consultorios) {
        for (Consultorio consultorio : consultorios) {
            if (!consultorio.isOccupied()) {
                animatePatientToConsultorio(patient, consultorio);
                consultorio.setOccupied(true);
                break;
            }
        }
    }

    private void createAndAnimatePatient(int patientNumber, Pane mainLayout, Rectangle receptionist, List<Consultorio> consultorios) {
        Rectangle patient = new Rectangle(20, 20, Color.BLUE);
        patient.setLayoutX(10); // Posición inicial X (esquina superior izquierda)
        patient.setLayoutY(10); // Posición inicial Y
        mainLayout.getChildren().add(patient);

        animatePatientToReception(patient, receptionist, consultorios, 5 * patientNumber);
    }

    private void schedulePatientCreation(PatientMonitor monitor, Pane mainLayout, Rectangle receptionist, List<Consultorio> consultorios) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Esperar 5 segundos
                    Platform.runLater(() -> monitor.createAndAnimateNextPatient(mainLayout, receptionist, consultorios));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void animatePatientToConsultorio(Rectangle patient, Consultorio consultorio) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), patient);
        toConsultorio.setToX(consultorio.visualRepresentation.getLayoutX() - patient.getLayoutX());
        toConsultorio.setToY(consultorio.visualRepresentation.getLayoutY() - patient.getLayoutY());
        toConsultorio.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
