package hospital.simulator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.Random;

public class simulator extends Application {
    @Override
    public void start(Stage primaryStage) {
        Pane mainLayout = new Pane();
        mainLayout.getChildren().add(createReceptionArea());
        mainLayout.getChildren().add(createConsultationRooms());

        Rectangle patient = createPatient();
        mainLayout.getChildren().add(patient);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setTitle("Hospital Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        animatePatientToReception(patient, createReceptionArea());
    }

    private Pane createConsultationRooms() {
        Pane consultationRooms = new Pane();
        for (int i = 0; i < 10; i++) {
            Rectangle consultorio = new Rectangle(30, 30);
            consultorio.setLayoutX(150 + 40 * i); // Posicionamiento horizontal
            consultorio.setLayoutY(250); // Posicionamiento vertical en el centro
            consultationRooms.getChildren().add(consultorio);
        }
        return consultationRooms;
    }

    private Pane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createPatientArrivalArea());
        mainLayout.setLeft(createReceptionArea());
        mainLayout.setCenter(createConsultationRooms());
        mainLayout.setRight(createNurseWaitingArea());
        return mainLayout;
    }

    private VBox createPatientArrivalArea() {
        VBox patientArrivalArea = new VBox(10);
        patientArrivalArea.getChildren().add(new Label("Área de Llegada de Pacientes"));
        // Aquí puedes añadir más componentes como listas de pacientes, etc.
        return patientArrivalArea;
    }

    private Rectangle createReceptionArea() {
        Rectangle receptionArea = new Rectangle(100, 50); // Tamaño del área de recepción
        receptionArea.setLayoutX(10); // Posición X
        receptionArea.setLayoutY(200); // Posición Y (centro izquierda)
        return receptionArea;
    }

    private VBox createNurseWaitingArea() {
        VBox nurseWaitingArea = new VBox(10);
        nurseWaitingArea.getChildren().add(new Label("Área de Espera de Enfermeros"));
        // Espacio para mostrar enfermeros en espera o acciones relacionadas
        return nurseWaitingArea;
    }

    private Rectangle createPatient() {
        Rectangle patient = new Rectangle(20, 20, Color.BLUE);
        patient.setLayoutX(750); // Posición inicial X (esquina superior derecha)
        patient.setLayoutY(10); // Posición inicial Y
        return patient;
    }

    private void animatePatientToReception(Rectangle patient, Rectangle receptionArea) {
        TranslateTransition toReception = new TranslateTransition(Duration.seconds(3), patient);
        toReception.setToX(receptionArea.getLayoutX() - patient.getLayoutX());
        toReception.setToY(receptionArea.getLayoutY() - patient.getLayoutY());

        toReception.setOnFinished(event -> animatePatientToConsultorio(patient));
        toReception.play();
    }

    private void animatePatientToConsultorio(Rectangle patient) {
        // Escoger un consultorio al azar
        int consultorioIndex = new Random().nextInt(10);
        double consultorioX = 150 + 40 * consultorioIndex;
        double consultorioY = 250;

        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), patient);
        toConsultorio.setToX(consultorioX - patient.getLayoutX());
        toConsultorio.setToY(consultorioY - patient.getLayoutY());
        toConsultorio.play();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
