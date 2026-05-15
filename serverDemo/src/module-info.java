module serverDemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens client to javafx.fxml;
    opens serverGUI to javafx.fxml;
    opens serverController to javafx.fxml;
    opens common to javafx.base;
    
    
    exports client;
    exports server;
    exports serverCommon;
    exports serverController;
    exports serverGUI;
    exports databaseControllers;
}