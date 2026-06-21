package clientGUI;

import java.util.List;

import clientCommon.ClientSession;
import clientCommon.ParkParameterObserver;
import clientController.ClientController;
import common.OperationResponse;
import common.ParkParameterChangeRequest;
import common.Protocol;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controls the park parameter approval page.
 * 
 * This page is used by department managers.
 */
public class ParkParameterApprovalPageController implements ParkParameterObserver {

    private ClientController clientController;

    @FXML
    private Label headerLabel;

    @FXML
    private Label subHeaderLabel;

    @FXML
    private TableView<ParkParameterChangeRequest> requestsTableView;

    @FXML
    private TableColumn<ParkParameterChangeRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<ParkParameterChangeRequest, Integer> parkIdColumn;

    @FXML
    private TableColumn<ParkParameterChangeRequest, Integer> requestedByColumn;

    @FXML
    private TableColumn<ParkParameterChangeRequest, String> parameterColumn;

    @FXML
    private TableColumn<ParkParameterChangeRequest, String> oldValueColumn;

    @FXML
    private TableColumn<ParkParameterChangeRequest, String> newValueColumn;

    @FXML
    private TableColumn<ParkParameterChangeRequest, String> statusColumn;

    @FXML
    private TextArea reviewNoteArea;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Label statusLabel;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void initialize() {
        headerLabel.setText("Park Parameter Change Approval");
        subHeaderLabel.setText("Review pending park parameter change requests");

        setupTableColumns();
        setupSelectionListener();

        requestsTableView.setPlaceholder(
                new Label("No pending park parameter change requests")
        );

        approveButton.setDisable(true);
        rejectButton.setDisable(true);

        if (!"department_manager".equals(ClientSession.getEmployeeRole())) {
            statusLabel.setText("Status: Only department managers can review requests.");
            refreshButton.setDisable(true);
            return;
        }

        statusLabel.setText("Status: Ready");
    }

    private void setupTableColumns() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        parkIdColumn.setCellValueFactory(new PropertyValueFactory<>("parkId"));
        requestedByColumn.setCellValueFactory(new PropertyValueFactory<>("requestedByEmployeeId"));
        oldValueColumn.setCellValueFactory(new PropertyValueFactory<>("oldValue"));
        newValueColumn.setCellValueFactory(new PropertyValueFactory<>("newValue"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));

        parameterColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatParameterName(cellData.getValue().getParameterName())
                )
        );
    }

    private void setupSelectionListener() {
        requestsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedRequest) -> {

                    boolean noSelection = selectedRequest == null;
                    boolean notDepartmentManager =
                            !"department_manager".equals(ClientSession.getEmployeeRole());

                    approveButton.setDisable(noSelection || notDepartmentManager);
                    rejectButton.setDisable(noSelection || notDepartmentManager);
                }
        );
    }

    private String formatParameterName(String parameterName) {
        if (parameterName == null) {
            return "-";
        }

        switch (parameterName) {
        case "max_capacity":
            return "Maximum capacity";

        case "places_for_unplanned_visitors":
            return "Places for unplanned visitors";

        case "estimated_visit_duration_hours":
            return "Estimated visit duration (hours)";

        default:
            return parameterName;
        }
    }

    @FXML
    private void handleRefresh() {
        requestPendingRequests();
    }

    @FXML
    private void handleApprove() {
        ParkParameterChangeRequest selectedRequest =
                requestsTableView.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            showWarningAlert("No request selected", "Please select a request first.");
            statusLabel.setText("Status: Please select a request.");
            return;
        }

        if (clientController == null) {
            showErrorAlert("Client Error", "Client is not connected.");
            statusLabel.setText("Status: Client is not connected.");
            return;
        }

        String reviewNote = reviewNoteArea.getText();

        statusLabel.setText("Status: Approving request...");

        clientController.approveParkParameterChangeRequest(
                selectedRequest.getRequestId(),
                ClientSession.getEmployeeId(),
                reviewNote
        );
    }

    @FXML
    private void handleReject() {
        ParkParameterChangeRequest selectedRequest =
                requestsTableView.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            showWarningAlert("No request selected", "Please select a request first.");
            statusLabel.setText("Status: Please select a request.");
            return;
        }

        if (clientController == null) {
            showErrorAlert("Client Error", "Client is not connected.");
            statusLabel.setText("Status: Client is not connected.");
            return;
        }

        String reviewNote = reviewNoteArea.getText();

        statusLabel.setText("Status: Rejecting request...");

        clientController.rejectParkParameterChangeRequest(
                selectedRequest.getRequestId(),
                ClientSession.getEmployeeId(),
                reviewNote
        );
    }

    private void requestPendingRequests() {
        if (clientController == null) {
            statusLabel.setText("Status: Client is not connected.");
            return;
        }

        if (!ClientSession.isEmployeeLoggedIn()) {
            statusLabel.setText("Status: Employee is not logged in.");
            return;
        }

        if (!"department_manager".equals(ClientSession.getEmployeeRole())) {
            statusLabel.setText("Status: Only department managers can load pending requests.");
            return;
        }

        statusLabel.setText("Status: Loading pending requests...");

        clientController.requestPendingParkParameterChangeRequests(
                ClientSession.getEmployeeId()
        );
    }

    @Override
    public void onPendingParkParameterRequestsReceived(
            List<ParkParameterChangeRequest> requests) {

        ObservableList<ParkParameterChangeRequest> items =
                FXCollections.observableArrayList();

        if (requests != null) {
            items.addAll(requests);
        }

        requestsTableView.setItems(items);
        reviewNoteArea.clear();

        approveButton.setDisable(true);
        rejectButton.setDisable(true);

        if (items.isEmpty()) {
            statusLabel.setText("Status: No pending requests.");
        } else {
            statusLabel.setText("Status: " + items.size() + " pending requests loaded.");
        }
    }

    @Override
    public void onParkParameterOperationResponse(
            OperationResponse response, Protocol responseType) {

        if (response == null) {
            statusLabel.setText("Status: No response from server.");
            showErrorAlert("No Response", "No response was received from the server.");
            return;
        }

        statusLabel.setText("Status: " + response.getMessage());

        if (!response.isSuccess()) {
            showErrorAlert("Operation Failed", response.getMessage());
            return;
        }

        if (responseType == Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED) {
            showInfoAlert("Request Approved", response.getMessage());
            reviewNoteArea.clear();
            requestPendingRequests();
            return;
        }

        if (responseType == Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED) {
            showInfoAlert("Request Rejected", response.getMessage());
            reviewNoteArea.clear();
            requestPendingRequests();
            return;
        }

        showInfoAlert("Operation Completed", response.getMessage());
    }


    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkParameterApprovalPage] " + message);
    }

    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkParameterApprovalPage] SUCCESS - " + message);
    }

    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkParameterApprovalPage] ERROR - " + message);
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