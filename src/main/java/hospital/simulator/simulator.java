package hospital.simulator;
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

public class simulator extends Application {
    Pane mainLayout = new Pane();

    private BlockingQueue<Consultorio> doctorReviewBuffer = new LinkedBlockingQueue<>();

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
        DoctorPatientMonitor monitor = new DoctorPatientMonitor(doctors, consultorios);

        // Programar la creación y animación de pacientes
        schedulePatientCreation(monitor, mainLayout, receptionist, consultorios, doctors);

        processDoctorReviews(monitor);

        // Configurar y mostrar la escena
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setTitle("Hospital Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Random random = new Random();


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

    public class NurseMonitor {
        private List<Rectangle> availableNurses;

        public NurseMonitor(List<Rectangle> nurses) {
            this.availableNurses = new LinkedList<>(nurses);
        }

        public synchronized void assignNurseToConsultorio(Consultorio consultorio, Runnable onFinished) {
            if (!availableNurses.isEmpty()) {
                Rectangle nurse = availableNurses.remove(0);
                Platform.runLater(() -> {
                    animateNurseToConsultorio(nurse, consultorio, () -> {
                        availableNurses.add(nurse);
                        animateNurseBack(nurse);
                        onFinished.run(); // Ejecuta la acción adicional aquí
                    });
                });
            }
        }
    }

    public class DoctorPatientMonitor {
        private Queue<Rectangle> availableDoctors;
        private List<Consultorio> consultorios;

        public DoctorPatientMonitor(List<Rectangle> doctors, List<Consultorio> consultorios) {
            this.availableDoctors = new LinkedList<>(doctors);
            this.consultorios = consultorios;
        }

        public synchronized void assignDoctorToPatient(Rectangle patient, List<Consultorio> consultorios) {
            int index = random.nextInt(this.consultorios.size());
            Consultorio consultorio = this.consultorios.get(index);
            if (!consultorio.isOccupied() && !availableDoctors.isEmpty()) {
                Rectangle doctor = availableDoctors.remove();
                animatePatientToConsultorio(patient, consultorio);
                animateDoctorToConsultorio(doctor, consultorio, () -> {
                    // Devolver el doctor a la cola de disponibles
                    availableDoctors.add(doctor);
                    animateDoctorBack(doctor);
                });
                consultorio.setOccupied(true);
                consultorio.setAssignedDoctor(doctor);
                consultorio.setAssignedPatient(patient); // Asegúrate de que esta línea esté en tu código
            }
        }

        public synchronized void assignDoctorToReview(Consultorio consultorio) {
            if (!availableDoctors.isEmpty()) {
                Rectangle doctor = availableDoctors.remove();
                Platform.runLater(() -> {
                    animateDoctorToConsultorio(doctor, consultorio, () -> {
                        animatePatientOut(consultorio.getAssignedPatient());
                        availableDoctors.add(doctor);
                        animateDoctorBack(doctor);
                        markConsultorioAsAvailable(consultorio);
                    });
                });
            }
        }

        private void markConsultorioAsAvailable(Consultorio consultorio) {
            consultorio.setOccupied(false);
            consultorio.setAssignedDoctor(null);
            consultorio.setAssignedPatient(null);
        }
    }

    class Consultorio {
        private Rectangle assignedPatient;
        private Rectangle visualRepresentation;
        private boolean isOccupied;
        private Rectangle assignedDoctor; // Referencia al doctor asignado

        Consultorio(int x, int y) {
            visualRepresentation = new Rectangle(30, 30);
            visualRepresentation.setLayoutX(x);
            visualRepresentation.setLayoutY(y);
            isOccupied = false;
            assignedDoctor = null;
        }

        public Rectangle getVisualRepresentation() {
            return visualRepresentation;
        }

        public boolean isOccupied() {
            return isOccupied;
        }

        public void setOccupied(boolean occupied) {
            isOccupied = occupied;
        }

        public Rectangle getAssignedDoctor() {
            return assignedDoctor;
        }

        public void setAssignedDoctor(Rectangle assignedDoctor) {
            this.assignedDoctor = assignedDoctor;
        }

        public Rectangle getAssignedPatient() {
            return assignedPatient;
        }

        public void setAssignedPatient(Rectangle assignedPatient) {
            this.assignedPatient = assignedPatient;
        }
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

    private void animateNurseToConsultorio(Rectangle nurse, Consultorio consultorio, Runnable onFinished) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), nurse);
        toConsultorio.setToX(consultorio.visualRepresentation.getLayoutX() - nurse.getLayoutX());
        toConsultorio.setToY(consultorio.visualRepresentation.getLayoutY() - nurse.getLayoutY());

        toConsultorio.setOnFinished(event -> {
            new Thread(() -> {
                // Lógica de espera
                Platform.runLater(() -> {
                    onFinished.run();
                    animateNurseBack(nurse);
                });
            }).start();
        });
        toConsultorio.play();
    }

    private void animateNurseBack(Rectangle nurse) {
        TranslateTransition backToPlace = new TranslateTransition(Duration.seconds(3), nurse);
        backToPlace.setToX(0);
        backToPlace.setToY(0);
        backToPlace.play();
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
    private Rectangle findAvailableNurse(List<Rectangle> nurses) {
        // Implementa lógica para encontrar un enfermero disponible
        return null;
    }

    private void animatePatientOut(Rectangle patient) {
        if (patient != null) {
            TranslateTransition exitTransition = new TranslateTransition(Duration.seconds(3), patient);
            exitTransition.setToY(-50); // Mueve al paciente fuera de la escena por arriba
            exitTransition.setOnFinished(event -> patient.setVisible(false)); // Oculta el paciente
            exitTransition.play();
        }
    }

    private void createAndAnimatePatient(int patientNumber, Pane mainLayout, Rectangle receptionist, DoctorPatientMonitor monitor, List<Consultorio> consultorios, List<Rectangle> doctors) {
        Rectangle patient = new Rectangle(20, 20, Color.BLUE);
        patient.setLayoutX(10); // Posición inicial X (esquina superior izquierda)
        patient.setLayoutY(10); // Posición inicial Y
        mainLayout.getChildren().add(patient);

        animatePatientToReception(patient, receptionist, monitor, consultorios, doctors);
    }
    private void animatePatientToConsultorio(Rectangle patient, Consultorio consultorio) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), patient);
        toConsultorio.setToX(consultorio.visualRepresentation.getLayoutX() - patient.getLayoutX());
        toConsultorio.setToY(consultorio.visualRepresentation.getLayoutY() - patient.getLayoutY());
        toConsultorio.play();
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

    private void animateDoctorToConsultorio(Rectangle doctor, Consultorio consultorio, Runnable onFinished) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), doctor);
        toConsultorio.setDelay(Duration.seconds(2)); // 2 segundos antes de moverse
        toConsultorio.setToX(consultorio.visualRepresentation.getLayoutX() - doctor.getLayoutX());
        toConsultorio.setToY(consultorio.visualRepresentation.getLayoutY() - doctor.getLayoutY());

        toConsultorio.setOnFinished(event -> {
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // El doctor pasa tiempo en el consultorio
                    Platform.runLater(() -> {
                        onFinished.run();
                        animateDoctorBack(doctor);
                        sendPatientToBuffer(consultorio); // Envía al paciente al buffer después de la visita del doctor
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
        toConsultorio.play();
    }

    private void animateDoctorBack(Rectangle doctor) {
        TranslateTransition backToPlace = new TranslateTransition(Duration.seconds(3), doctor);
        backToPlace.setToX(0);
        backToPlace.setToY(0);
        backToPlace.setOnFinished(event -> {
            // Aquí puedes realizar acciones adicionales si es necesario
        });
        backToPlace.play();
    }

    private BlockingQueue<Rectangle> patientBuffer = new LinkedBlockingQueue<>();

    private void sendPatientToBuffer(Consultorio consultorio) {
        try {
            patientBuffer.put(consultorio.getAssignedPatient()); // Usa la referencia al paciente
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
