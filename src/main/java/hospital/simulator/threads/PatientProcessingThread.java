package hospital.simulator.threads;

import hospital.simulator.models.Consultorio;
import hospital.simulator.monitors.NurseMonitor;
import javafx.scene.image.ImageView;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class PatientProcessingThread implements Runnable {
    private final NurseMonitor nurseMonitor;
    private final List<Consultorio> consultorios;
    private final BlockingQueue<ImageView> patientBuffer;
    private final BlockingQueue<Consultorio> doctorReviewBuffer;

    public PatientProcessingThread(NurseMonitor nurseMonitor, List<Consultorio> consultorios, BlockingQueue<ImageView> patientBuffer, BlockingQueue<Consultorio> doctorReviewBuffer) {
        this.nurseMonitor = nurseMonitor;
        this.consultorios = consultorios;
        this.patientBuffer = patientBuffer;
        this.doctorReviewBuffer = doctorReviewBuffer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ImageView patient = patientBuffer.take();
                Consultorio assignedConsultorio = findConsultorioWithPatient(consultorios, patient);
                if (assignedConsultorio != null) {
                    nurseMonitor.assignNurseToConsultorio(assignedConsultorio, () -> {
                        try {
                            doctorReviewBuffer.put(assignedConsultorio);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Consultorio findConsultorioWithPatient(List<Consultorio> consultorios, ImageView patient) {
        for (Consultorio consultorio : consultorios) {
            if (consultorio.getAssignedPatient() == patient) {
                return consultorio;
            }
        }
        return null;
    }
}
