
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
import common.Employee;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Displays real-time park visitor counters.
 *
 * Department manager can view all parks.
 * Park manager can view only his own park.
 */
public class ParkVisitorCounterViewPageController implements ParkVisitorCounterObserver {

	/**
	 * the role name of a park worker
	 */
	private static final String ROLE_PARK_WORKER = "park_worker";

	/**
	 * the role name of a park manager
	 */
	private static final String ROLE_PARK_MANAGER = "park_manager";

	/**
	 * the role name of a department manager
	 */
	private static final String ROLE_DEPARTMENT_MANAGER = "department_manager";

    /**
     * the client controller used to communicate with the server
     */
    private ClientController clientController;
    
    /**
     * the currently logged-in employee
     */
    private Employee loggedInEmployee;

    /**
     * the table view that displays park visitor counters
     */
    @FXML
    private TableView<ParkVisitorCounterSnapshot> counterTableView;

    /**
     * the column that displays the park name
     */
    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, String> parkNameColumn;

    /**
     * the column that displays the current number of visitors
     */
    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, Integer> currentVisitorsColumn;

    /**
     * the column that displays the maximum park capacity
     */
    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, Integer> maxCapacityColumn;

    /**
     * the column that displays the number of available places
     */
    @FXML
    private TableColumn<ParkVisitorCounterSnapshot, Integer> availablePlacesColumn;

    /**
     * the button used to refresh the visitor counters
     */
    @FXML
    private Button refreshButton;

    /**
     * the label used to display status messages
     */
    @FXML
    private Label statusLabel;

    /**
     * Sets the client controller.
     *
     * @param clientController the client controller
     */
    public void setClientController(ClientController clientController) {
    	this.clientController = clientController;

    	if (this.clientController != null) {
    		this.clientController.addParkVisitorCounterObserver(this);
    		requestCounters();
    	}
    }
    
    /**
     * Sets the logged-in employee.
     *
     * @param employee the logged-in employee
     */
    public void setLoggedInEmployee(Employee employee) {
    	this.loggedInEmployee = employee;
    }

    /**
     * Initializes the visitor counter view page.
     *
     * This method sets the table columns, placeholder,
     * and checks whether the current employee can view the page.
     */
    @FXML
    private void initialize() {
        setupTableColumns();

        counterTableView.setPlaceholder(
                new Label("No visitor counter data to display")
        );

        if (!canCurrentEmployeeViewCounters()) {
            refreshButton.setDisable(true);
            setErrorStatus("Only park workers, park managers and department managers can view this page.");
            return;
        }

        setInfoStatus("Ready");
    }

    /**
     * Sets the table column value factories.
     */
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

    /**
     * Checks whether the current employee can view visitor counters.
     *
     * @return true if the current employee can view visitor counters
     */
    private boolean canCurrentEmployeeViewCounters() {
    	if (!isTestMode() && !ClientSession.isEmployeeLoggedIn()) {
    		return false;
    	}

    	String role = getCurrentEmployeeRole();

    	return ROLE_PARK_WORKER.equals(role)
    			|| ROLE_PARK_MANAGER.equals(role)
    			|| ROLE_DEPARTMENT_MANAGER.equals(role);
    }

    /**
     * Handles the click on the refresh button.
     */
    @FXML
    private void handleRefresh() {
        requestCounters();
    }

    /**
     * Requests visitor counter data from the server.
     */
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

    /**
     * This method is called when park visitor counters are received from the server.
     *
     * @param counters the list of visitor counter snapshots
     */
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

    /**
     * This method is called when the server returns a visitor counter operation response.
     *
     * @param response the operation response received from the server
     * @param responseType the protocol type of the response
     */
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

    /**
     * This method is called when park visitor counters are updated.
     */
    @Override
    public void onParkVisitorCountersUpdated() {
        requestCounters();
    }

    /**
     * Checks whether visitor counter test mode is active.
     *
     * @return true if test mode is active
     */
    private boolean isTestMode() {
        return Boolean.getBoolean("visitorCounterTestMode");
    }

    /**
     * Returns the current employee role.
     *
     * @return the current employee role
     */
    private String getCurrentEmployeeRole() {
        if (isTestMode()) {
            return System.getProperty("visitorCounterTestRole", "");
        }

        return ClientSession.getEmployeeRole();
    }

    /**
     * Returns the current employee ID.
     *
     * @return the current employee ID
     */
    private int getCurrentEmployeeId() {
        if (isTestMode()) {
            return Integer.getInteger("visitorCounterTestEmployeeId", -1);
        }

        return ClientSession.getEmployeeId();
    }

    /**
     * Sets an information status message.
     *
     * @param message the status message
     */
    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkVisitorCounterViewPage] " + message);
    }

    /**
     * Sets a success status message.
     *
     * @param message the status message
     */
    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkVisitorCounterViewPage] SUCCESS - " + message);
    }

    /**
     * Sets an error status message.
     *
     * @param message the status message
     */
    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkVisitorCounterViewPage] ERROR - " + message);
    }

    /**
     * Updates the status label text and style.
     *
     * @param message the status message
     * @param statusStyleClass the status style class
     */
    private void updateStatusLabel(String message, String statusStyleClass) {
        statusLabel.setText("Status: " + message);

        statusLabel.getStyleClass().remove("status-info");
        statusLabel.getStyleClass().remove("status-success");
        statusLabel.getStyleClass().remove("status-error");

        if (!statusLabel.getStyleClass().contains(statusStyleClass)) {
            statusLabel.getStyleClass().add(statusStyleClass);
        }
    }
    
    /**
     * Handles the click on the back button.
     *
     * This method returns the user to the correct dashboard
     * according to the current employee role.
     *
     * @param event the button click event
     */
    @FXML
    private void handleBack(ActionEvent event) {
    	try {
    		String role = ClientSession.getEmployeeRole();

    		String fxmlPath;
    		String title;

    		if ("department_manager".equals(role)) {
    			fxmlPath = "/clientGUI/DepartmentManagerHomePage.fxml";
    			title = "Department Manager Dashboard";
    		} else if ("park_manager".equals(role)) {
    			fxmlPath = "/clientGUI/ParkManagerHomePage.fxml";
    			title = "Park Manager Dashboard";
    		} else if ("park_worker".equals(role)) {
    			fxmlPath = "/clientGUI/ParkWorkerHomePage.fxml";
    			title = "Park Worker Dashboard";
    		} else {
    			setErrorStatus("Cannot return back: unknown employee role.");
    			return;
    		}

    		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    		Parent root = loader.load();

    		Object controller = loader.getController();

    		if (controller instanceof DepartmentManagerHomePageController) {
    			DepartmentManagerHomePageController departmentController =
    					(DepartmentManagerHomePageController) controller;
    			departmentController.setClientController(clientController);
    			departmentController.setLoggedInEmployee(loggedInEmployee);
    		} else if (controller instanceof ParkManagerHomePageController) {
    			ParkManagerHomePageController parkManagerController =
    					(ParkManagerHomePageController) controller;
    			parkManagerController.setClientController(clientController);
    			parkManagerController.setLoggedInEmployee(loggedInEmployee);
    		} else if (controller instanceof ParkWorkerHomePageController) {
    			ParkWorkerHomePageController parkWorkerController =
    					(ParkWorkerHomePageController) controller;
    			parkWorkerController.setClientController(clientController);
    			parkWorkerController.setLoggedInEmployee(loggedInEmployee);
    		}

    		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    		stage.setTitle(title);
    		stage.setScene(new Scene(root));
    		stage.show();

    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}

