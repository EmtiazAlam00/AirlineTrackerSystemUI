module airlinetrackersystem.airlinetrackerfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens airlinetrackersystem.airlinetrackerfx to javafx.fxml;
    exports airlinetrackersystem.airlinetrackerfx;
}