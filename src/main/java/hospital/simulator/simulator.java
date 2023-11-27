package hospital.simulator;
import javafx.application.Application;
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

public class simulator extends Application {
    @Override
    public void start(Stage primaryStage) {
        Pane mainLayout = new Pane();
        Rectangle receptionist = createReceptionist();
        mainLayout.getChildren().add(receptionist);

        List<Consultorio> consultorios = createConsultationRooms();
        consultorios.forEach(c -> mainLayout.getChildren().add(c.getVisualRepresentation()));

        List<Rectangle> patients = createPatients(10);
        for (int i = 0; i < patients.size(); i++) {
            Rectangle patient = patients.get(i);
            mainLayout.getChildren().add(patient);
            animatePatientToReception(patient, receptionist, consultorios, 5 * i); // Retraso secuencial
        }

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setTitle("Hospital Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Rectangle createReceptionist() {
        Rectangle receptionist = new Rectangle(20, 20, Color.YELLOW);
        receptionist.setLayoutX(10); // Posición de la recepcionista
        receptionist.setLayoutY(200);
        return receptionist;
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
        for (int i = 0; i < 10; i++) {
            Consultorio consultorio = new Consultorio(100 + 50 * i, 300); // Posicionamiento en el centro
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
