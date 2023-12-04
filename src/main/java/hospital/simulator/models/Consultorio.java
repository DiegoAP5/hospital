package hospital.simulator.models;

import javafx.scene.image.ImageView;

public class Consultorio {
    private ImageView assignedPatient;
    private ImageView visualRepresentation;
    private boolean isOccupied;
    private ImageView assignedDoctor;

    public Consultorio(int x, int y) {
        visualRepresentation = new ImageView("file:src/main/resources/assets/images/clinica.png");
        visualRepresentation.setLayoutX(x);
        visualRepresentation.setLayoutY(y);
        visualRepresentation.setFitHeight(30);
        visualRepresentation.setFitWidth(30);
        isOccupied = false;
        assignedDoctor = null;
    }

    public ImageView getVisualRepresentation() {
        return visualRepresentation;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public void setAssignedDoctor(ImageView assignedDoctor) {
        this.assignedDoctor = assignedDoctor;
    }

    public ImageView getAssignedPatient() {
        return assignedPatient;
    }

    public void setAssignedPatient(ImageView assignedPatient) {
        this.assignedPatient = assignedPatient;
    }
}