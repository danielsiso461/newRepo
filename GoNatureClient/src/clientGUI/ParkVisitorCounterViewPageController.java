package clientGUI;

import java.util.List;

import clientCommon.ClientSession;
import clientCommon.ParkVisitorCounterObserver;
import clientController.ClientController;
import common.OperationResponse;
import common.ParkVisitorCounterSnapshot;
import common.Protocol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Displays real-time park visitor counters.
 *
 * Department manager can view all parks.
 * Park manager can view only his own park.
 */
public class ParkVisitorCounterViewPageController implements ParkVisitorCounterObserver {

    private static final String ROLE_PARK_MANAGER = "park_manager";
    private static final String ROLE_DEPARTMENT_MANAGER = "department_manager";

    private ClientController clientController;

    @FXML
    private TableView<ParkVisitorCounterSnapshot> counterTableView;

    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, String> parkNameColumn;

    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, Integer> currentVisitorsColumn;

    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, Integer> maxCapacityColumn;

    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, Integer> availablePlacesColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Label statusLabel;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void initialize() {
        setupTableColumns();

        counterTableView.setPlaceholder(
                new Label("No visitor counter data to display")
        );

        if (!canCurrentEmployeeViewCounters()) {
            refreshButton.setDisable(true);
            setErrorStatus("Only park managers and department managers can view this page.");
            return;
        }

        setInfoStatus("Ready");
    }

    private void setupTableColumns() {
        parkNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("parkName")
        );

        currentVisitorsColumn.setCellValueFactory(
                new PropertyValueFactory<>("currentVisitors")
        );

        maxCapacityColumn.setCellValueFactory(
                new PropertyValueFactory<>("maxCapacity")
        );

        availablePlacesColumn.setCellValueFactory(
                new PropertyValueFactory<>("availablePlaces")
        );

        counterTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private boolean canCurrentEmployeeViewCounters() {
        if (!isTestMode() && !ClientSession.isEmployeeLoggedIn()) {
            return false;
        }

        String role = getCurrentEmployeeRole();

        return ROLE_PARK_MANAGER.equals(role)
                || ROLE_DEPARTMENT_MANAGER.equals(role);
    }

    @FXML
    private void handleRefresh() {
        requestCounters();
    }

    private void requestCounters() {
        if (clientController == null) {
            setErrorStatus("Client is not connected.");
            return;
        }

        if (!canCurrentEmployeeViewCounters()) {
            setErrorStatus("You are not allowed to view visitor counters.");
            return;
        }

        int employeeId = getCurrentEmployeeId();

        if (employeeId <= 0) {
            setErrorStatus("Invalid employee id.");
            return;
        }

        setInfoStatus("Loading visitor counters...");

        clientController.requestParkVisitorCounters(employeeId);
    }

    @Override
    public void onParkVisitorCountersReceived(
            List<ParkVisitorCounterSnapshot> counters) {

        ObservableList<ParkVisitorCounterSnapshot> items =
                FXCollections.observableArrayList();

        if (counters != null) {
            items.addAll(counters);
        }

        counterTableView.setItems(items);

        if (items.isEmpty()) {
            setInfoStatus("No visitor counter data found.");
        } else {
            setSuccessStatus("Visitor counters loaded successfully.");
        }
    }

    @Override
    public void onParkVisitorCounterOperationResponse(
            OperationResponse response, Protocol responseType) {

        if (response == null) {
            setErrorStatus("No response was received from the server.");
            return;
        }

        if (!response.isSuccess()) {
            setErrorStatus(response.getMessage());
            return;
        }

        if (responseType == Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT) {
            setSuccessStatus(response.getMessage());
            requestCounters();
        }
    }

    @Override
    public void onParkVisitorCountersUpdated() {
        requestCounters();
    }

    private boolean isTestMode() {
        return Boolean.getBoolean("visitorCounterTestMode");
    }

    private String getCurrentEmployeeRole() {
        if (isTestMode()) {
            return System.getProperty("visitorCounterTestRole", "");
        }

        return ClientSession.getEmployeeRole();
    }

    private int getCurrentEmployeeId() {
        if (isTestMode()) {
            return Integer.getInteger("visitorCounterTestEmployeeId", -1);
        }

        return ClientSession.getEmployeeId();
    }

    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkVisitorCounterViewPage] " + message);
    }

    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkVisitorCounterViewPage] SUCCESS - " + message);
    }

    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkVisitorCounterViewPage] ERROR - " + message);
    }

    private void updateStatusLabel(String message, String statusStyleClass) {
        statusLabel.setText("Status: " + message);

        statusLabel.getStyleClass().remove("status-info");
        statusLabel.getStyleClass().remove("status-success");
        statusLabel.getStyleClass().remove("status-error");

        if (!statusLabel.getStyleClass().contains(statusStyleClass)) {
            statusLabel.getStyleClass().add(statusStyleClass);
        }
    }
}