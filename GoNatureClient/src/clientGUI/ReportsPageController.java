package clientGUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReportsPageController {

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private ComboBox<String> visitorTypeComboBox;

    @FXML
    private Button showReportButton;

    @FXML
    private TableView<?> reportsTableView;

    @FXML
    private TableColumn<?, ?> visitorIdColumn;

    @FXML
    private TableColumn<?, ?> visitorNameColumn;

    @FXML
    private TableColumn<?, ?> visitorTypeColumn;

    @FXML
    private TableColumn<?, ?> entryTimeColumn;

    @FXML
    private TableColumn<?, ?> exitTimeColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        reportTypeComboBox.getItems().addAll(
                "Visitor Report",
                "Treatment Report"
        );

        visitorTypeComboBox.getItems().addAll(
                "All",
                "Student",
                "Guide",
                "Guest",
                "Employee"
        );

        reportTypeComboBox.setValue("Visitor Report");
        visitorTypeComboBox.setValue("All");

        statusLabel.setText("Status: Ready");
    }

    @FXML
    private void handleShowReport() {
        String reportType = reportTypeComboBox.getValue();
        String visitorType = visitorTypeComboBox.getValue();

        System.out.println("Report Type: " + reportType);
        System.out.println("Visitor Type: " + visitorType);

        statusLabel.setText("Status: Report request sent");
    }
}