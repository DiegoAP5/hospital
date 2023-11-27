package hospital.simulator;
import hospital.simulator.models.Consultorio;
import hospital.simulator.monitors.DoctorPatientMonitor;
import hospital.simulator.monitors.NurseMonitor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class  extends Application {
    Pane mainLayout = new Pane();

    private BlockingQueue<Consultorio> doctorReviewBuffer = new LinkedBlockingQueue<>();
    private BlockingQueue<Rectangle> patientBuffer = new LinkedBlockingQueue<>();


    @Override
    public void start(Stage primaryStage) {
        Pane mainLayout = new Pane();

        // Crear la recepcionista
        Rectangle receptionist = createReceptionist();
        mainLayout.getChildren().add(receptionist);

        // Crear los consultorios
        List<Consultorio> consultorios = createConsultationRooms();
        for (Consultorio consultorio : consultorios) {
            mainLayout.getChildren().add(consultorio.getVisualRepresentation());
        }

        // Crear los médicos
        List<Rectangle> doctors = createDoctors();
        for (Rectangle doctor : doctors) {
            mainLayout.getChildren().add(doctor);
        }

        List<Rectangle> nurses = createNurses();
        nurses.forEach(nurse -> mainLayout.getChildren().add(nurse));

        NurseMonitor nurseMonitor = new NurseMonitor(nurses);
        processPatientsFromBuffer(nurseMonitor, consultorios);

        // Inicializar el monitor para pacientes y doctores
        DoctorPatientMonitor monitor = new DoctorPatientMonitor(doctors, consultorios, patientBuffer);

        // Programar la creación y animación de pacientes
        schedulePatientCreation(monitor, mainLayout, receptionist, consultorios, doctors);

        processDoctorReviews(monitor);

        // Configurar y mostrar la escena
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setTitle("Hospital Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<Rectangle> createNurses() {
        List<Rectangle> nurses = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Asumiendo que hay 5 enfermeros, ajusta según sea necesario
            Rectangle nurse = new Rectangle(15, 15, Color.GREEN);
            nurse.setLayoutX(950); // Posición X, ajusta según el diseño de tu interfaz
            nurse.setLayoutY(300 + (i * 30)); // Posición Y, espaciados verticalmente
            nurses.add(nurse);
        }
        return nurses;
    }

    private Rectangle createReceptionist() {
        Rectangle receptionist = new Rectangle(50, 20, Color.YELLOW); // Rectángulo más ancho
        receptionist.setLayoutX(10); // Posición de la recepcionista
        receptionist.setLayoutY(200);
        return receptionist;
    }

    private List<Consultorio> createConsultationRooms() {
        List<Consultorio> consultationRooms = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            // Asumiendo un tamaño de ventana de 1000x800
            Consultorio consultorio = new Consultorio(100 + (i % 10) * 80, 100 + (i / 10) * 60);
            consultationRooms.add(consultorio);
        }
        return consultationRooms;
    }

    private void animatePatientToReception(Rectangle patient, Rectangle receptionist, DoctorPatientMonitor monitor, List<Consultorio> consultorios, List<Rectangle> doctors) {
        TranslateTransition toReception = new TranslateTransition(Duration.seconds(3), patient);
        toReception.setToX(receptionist.getLayoutX() - patient.getLayoutX());
        toReception.setToY(receptionist.getLayoutY() - patient.getLayoutY());

        toReception.setOnFinished(event -> monitor.assignDoctorToPatient(patient, consultorios));
        toReception.play();
    }

    private void schedulePatientCreation(DoctorPatientMonitor monitor, Pane mainLayout, Rectangle receptionist, List<Consultorio> consultorios, List<Rectangle> doctors) {
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                try {
                    Thread.sleep(5000); // Esperar 5 segundos entre cada paciente
                    int finalI = i;
                    Platform.runLater(() -> createAndAnimatePatient(finalI, mainLayout, receptionist, monitor, consultorios, doctors));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void processPatientsFromBuffer(NurseMonitor nurseMonitor, List<Consultorio> consultorios) {
        new Thread(() -> {
            while (true) {
                try {
                    Rectangle patient = patientBuffer.take();
                    Consultorio assignedConsultorio = findConsultorioWithPatient(consultorios, patient);
                    if (assignedConsultorio != null) {
                        nurseMonitor.assignNurseToConsultorio(assignedConsultorio, () -> {
                            // Aquí va la lógica que se ejecutará después de la visita del enfermero
                            try {
                                doctorReviewBuffer.put(assignedConsultorio); // Envía el consultorio al buffer de revisión del doctor
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void processDoctorReviews(DoctorPatientMonitor doctorMonitor) {
        new Thread(() -> {
            while (true) {
                try {
                    Consultorio consultorio = doctorReviewBuffer.take(); // Espera hasta que haya un consultorio en el buffer
                    doctorMonitor.assignDoctorToReview(consultorio);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private Consultorio findConsultorioWithPatient(List<Consultorio> consultorios, Rectangle patient) {
        for (Consultorio consultorio : consultorios) {
            if (consultorio.getAssignedPatient() == patient) {
                return consultorio;
            }
        }
        return null;
    }

    private void createAndAnimatePatient(int patientNumber, Pane mainLayout, Rectangle receptionist, DoctorPatientMonitor monitor, List<Consultorio> consultorios, List<Rectangle> doctors) {
        Rectangle patient = new Rectangle(20, 20, Color.BLUE);
        patient.setLayoutX(10); // Posición inicial X (esquina superior izquierda)
        patient.setLayoutY(10); // Posición inicial Y
        mainLayout.getChildren().add(patient);

        animatePatientToReception(patient, receptionist, monitor, consultorios, doctors);
    }

    private List<Rectangle> createDoctors() {
        List<Rectangle> doctors = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Rectangle doctor = new Rectangle(15, 15, Color.RED);
            doctor.setLayoutX(250 + 20 * i); // Espaciado horizontal
            doctor.setLayoutY(720); // En el borde inferior
            doctors.add(doctor);
            mainLayout.getChildren().add(doctor);
        }
        return doctors;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
