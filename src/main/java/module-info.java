module hospital.simulator {
    requires javafx.controls;
    requires javafx.fxml;


    opens hospital.simulator to javafx.fxml;
    exports hospital.simulator;
    exports hospital.simulator.controllers;
    opens hospital.simulator.controllers to javafx.fxml;
}