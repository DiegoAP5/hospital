package hospital.simulator.monitors;

import hospital.simulator.models.Consultorio;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class DoctorPatientMonitor {
    private Queue<ImageView> availableDoctors;
    private List<Consultorio> consultorios;
    private BlockingQueue<ImageView> patientBuffer;
    private Random random = new Random();

    public DoctorPatientMonitor(List<ImageView> doctors, List<Consultorio> consultorios, BlockingQueue<ImageView> patientBuffer) {
        this.availableDoctors = new LinkedList<>(doctors);
        this.consultorios = consultorios;
        this.patientBuffer = patientBuffer;
    }

    public synchronized void assignDoctorToPatient(ImageView patient, List<Consultorio> consultorios) {
        int index = random.nextInt(this.consultorios.size());
        Consultorio consultorio = this.consultorios.get(index);
        if (!consultorio.isOccupied() && !availableDoctors.isEmpty()) {
            ImageView doctor = availableDoctors.remove();
            animatePatientToConsultorio(patient, consultorio);
            animateDoctorToConsultorio(doctor, consultorio, () -> {
                availableDoctors.add(doctor);
                animateDoctorBack(doctor);
            });
            consultorio.setOccupied(true);
            consultorio.setAssignedDoctor(doctor);
            consultorio.setAssignedPatient(patient);
        }
    }

    public synchronized void assignDoctorToReview(Consultorio consultorio) {
        if (!availableDoctors.isEmpty()) {
            ImageView doctor = availableDoctors.remove();
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

    private void animatePatientOut(ImageView patient) {
        if (patient != null) {
            TranslateTransition exitTransition = new TranslateTransition(Duration.seconds(3), patient);
            exitTransition.setToY(-50);
            exitTransition.setOnFinished(event -> patient.setVisible(false)); // Oculta el paciente
            exitTransition.play();
        }
    }

    private void animatePatientToConsultorio(ImageView patient, Consultorio consultorio) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), patient);
        toConsultorio.setToX(consultorio.getVisualRepresentation().getLayoutX() - patient.getLayoutX());
        toConsultorio.setToY(consultorio.getVisualRepresentation().getLayoutY() - patient.getLayoutY());
        toConsultorio.play();
    }

    private void animateDoctorToConsultorio(ImageView doctor, Consultorio consultorio, Runnable onFinished) {
        TranslateTransition toConsultorio = new TranslateTransition(Duration.seconds(3), doctor);
        toConsultorio.setDelay(Duration.seconds(2)); // 2 segundos antes de moverse
        toConsultorio.setToX(consultorio.getVisualRepresentation().getLayoutX() - doctor.getLayoutX());
        toConsultorio.setToY(consultorio.getVisualRepresentation().getLayoutY() - doctor.getLayoutY());

        toConsultorio.setOnFinished(event -> {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        onFinished.run();
                        animateDoctorBack(doctor);
                        sendPatientToBuffer(consultorio);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
        toConsultorio.play();
    }

    private void animateDoctorBack(ImageView doctor) {
        TranslateTransition backToPlace = new TranslateTransition(Duration.seconds(3), doctor);
        backToPlace.setToX(0);
        backToPlace.setToY(0);
        backToPlace.setOnFinished(event -> {
        });
        backToPlace.play();
    }

    private void sendPatientToBuffer(Consultorio consultorio) {
        try {
            patientBuffer.put(consultorio.getAssignedPatient());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}