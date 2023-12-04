package hospital.simulator;
import hospital.simulator.models.Consultorio;
import hospital.simulator.monitors.DoctorPatientMonitor;
import hospital.simulator.monitors.NurseMonitor;
import hospital.simulator.threads.DoctorReviewThread;
import hospital.simulator.threads.PatientCreationThread;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import hospital.simulator.threads.PatientProcessingThread;

public class GameRunner extends Application {
    Pane mainLayout = new Pane();
    private final BlockingQueue<Consultorio> doctorReviewBuffer = new LinkedBlockingQueue<>();
    private final BlockingQueue<ImageView> patientBuffer = new LinkedBlockingQueue<>();


    @Override
    public void start(Stage primaryStage) {
        Pane mainLayout = new Pane();

        Rectangle receptionist = createReceptionist();
        mainLayout.getChildren().add(receptionist);

        List<Consultorio> consultorios = createConsultationRooms();
        for (Consultorio consultorio : consultorios) {
            mainLayout.getChildren().add(consultorio.getVisualRepresentation());
        }

        List<ImageView> doctors = createDoctors();
        for (ImageView doctor : doctors) {
            mainLayout.getChildren().add(doctor);
        }

        List<ImageView> nurses = createNurses();
        nurses.forEach(nurse -> mainLayout.getChildren().add(nurse));

        NurseMonitor nurseMonitor = new NurseMonitor(nurses);

        PatientProcessingThread patientProcessingThread = new PatientProcessingThread(nurseMonitor, consultorios, patientBuffer, doctorReviewBuffer);
        Thread patientProcessingThreadInstance = new Thread(patientProcessingThread);
        patientProcessingThreadInstance.start();

        DoctorPatientMonitor monitor = new DoctorPatientMonitor(doctors, consultorios, patientBuffer);

        PatientCreationThread patientCreationThread = new PatientCreationThread(mainLayout, receptionist, monitor, consultorios, doctors);
        Thread patientCreationThreadInstance = new Thread(patientCreationThread);
        patientCreationThreadInstance.start();

        DoctorReviewThread doctorReviewThread = new DoctorReviewThread(monitor, doctorReviewBuffer);
        Thread doctorReviewThreadInstance = new Thread(doctorReviewThread);
        doctorReviewThreadInstance.start();

        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setTitle("Hospital Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<ImageView> createNurses() {
        List<ImageView> nurses = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ImageView nurse = new ImageView("file:src/main/resources/assets/images/nurse.png");
            nurse.setLayoutX(950);
            nurse.setLayoutY(300 + (i * 30));
            nurse.setFitHeight(20);
            nurse.setFitWidth(20);
            nurses.add(nurse);
        }
        return nurses;
    }

    private Rectangle createReceptionist() {
        Rectangle receptionist = new Rectangle(50, 20, Color.YELLOW);
        receptionist.setLayoutX(10);
        receptionist.setLayoutY(200);
        return receptionist;
    }

    private List<Consultorio> createConsultationRooms() {
        List<Consultorio> consultationRooms = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Consultorio consultorio = new Consultorio(100 + (i % 10) * 80, 100 + (i / 10) * 60);
            consultationRooms.add(consultorio);
        }
        return consultationRooms;
    }

    private List<ImageView> createDoctors() {
        List<ImageView> doctors = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ImageView doctor = new ImageView("file:src/main/resources/assets/images/doctor.png");
            doctor.setLayoutX(250 + 20 * i);
            doctor.setLayoutY(720);
            doctor.setFitHeight(25);
            doctor.setFitWidth(25);
            doctors.add(doctor);
            mainLayout.getChildren().add(doctor);
        }
        return doctors;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
