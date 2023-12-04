package hospital.simulator.threads;

import hospital.simulator.models.Consultorio;
import hospital.simulator.monitors.DoctorPatientMonitor;

import java.util.concurrent.BlockingQueue;

public class DoctorReviewThread implements Runnable {
    private final DoctorPatientMonitor doctorMonitor;
    private final BlockingQueue<Consultorio> doctorReviewBuffer;

    public DoctorReviewThread(DoctorPatientMonitor doctorMonitor, BlockingQueue<Consultorio> doctorReviewBuffer) {
        this.doctorMonitor = doctorMonitor;
        this.doctorReviewBuffer = doctorReviewBuffer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Consultorio consultorio = doctorReviewBuffer.take();
                doctorMonitor.assignDoctorToReview(consultorio);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
