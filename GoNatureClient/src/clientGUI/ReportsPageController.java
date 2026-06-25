package clientGUI;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import clientCommon.ClientSession;
import clientCommon.ParkObserver;
import clientCommon.ReportObserver;
import clientController.ClientController;
import common.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controls the reports screen.
 */
public class ReportsPageController implements ReportObserver, ParkObserver {
	/**
	 * Visitor report type.
	 */
    private static final String VISITOR_REPORT = "Visitor Report";
    /**
     * Cancellation report type.
     */
    private static final String CANCELLATION_REPORT = "Cancellation Report";
    /**
     * Visit duration report type.
     */
    private static final String VISIT_DURATION_REPORT = "Visit Duration Report";
    /**
     * Park usage report type.
     */
    private static final String PARK_USAGE_REPORT = "Park Usage Report";
    /**
     * Client controller used to communicate with the server.
     */
    private ClientController clientController;
    /**
     * The employee currently logged in.
     */
    private Employee loggedInEmployee;
    /** Report type selector. */
    @FXML
    private ComboBox<String> reportTypeComboBox;
    /** Park selector. */
    @FXML
    private ComboBox<Park> parkComboBox;
    /** Month selector. */
    @FXML
    private ComboBox<Integer> monthComboBox;
    /** Year selector. */
    @FXML
    private ComboBox<Integer> yearComboBox;
    /****
     * Shows the selected report.
     */
    @FXML
    private Button showReportButton;
    /** Displays the selected report. */
    @FXML
    private TableView<Object> reportsTableView;
    /**
     * Displays report data as a pie chart.
     */
    @FXML
    private PieChart reportPieChart;
    /** Displays report data as a bar chart. */
    @FXML
    private BarChart<String, Number> reportBarChart;
    /**
     * Bar chart category axis.
     */
    @FXML
    private CategoryAxis barChartXAxis;
    /**
    * Bar chart value axis.
    */
    @FXML
    private NumberAxis barChartYAxis;
    /** Displays report status messages. */
    @FXML
    private Label statusLabel;
    /**
     * Sets the client controller and registers this controller as an observer.
     *
     * @param clientController the client controller to use
     */
    public void setClientController(ClientController clientController) {
    	this.clientController = clientController;

    	if (this.clientController != null) {
    		this.clientController.addReportObserver(this);
    		this.clientController.addParkObserver(this);
    		this.clientController.requestActiveParks();
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
     * Initializes the reports page.
     */
    @FXML
    private void initialize() {
        initReportTypes();
        initDateFilters();

        reportsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        reportPieChart.setLegendVisible(true);
        reportPieChart.setLabelsVisible(true);

        reportBarChart.setLegendVisible(true);
        reportBarChart.setAnimated(false);

        hideCharts();

        statusLabel.setText("Status: Ready");
    }
    /**
     * Initializes the available report types.
     */
    private void initReportTypes() {
        reportTypeComboBox.getItems().clear();

        if ("park_manager".equals(ClientSession.getEmployeeRole())) {
            reportTypeComboBox.getItems().addAll(
                    VISITOR_REPORT,
                    CANCELLATION_REPORT
            );
        } else if ("department_manager".equals(ClientSession.getEmployeeRole())) {
            reportTypeComboBox.getItems().addAll(
                    VISITOR_REPORT,
                    CANCELLATION_REPORT,
                    VISIT_DURATION_REPORT,
                    PARK_USAGE_REPORT
            );
        }

        if (!reportTypeComboBox.getItems().isEmpty()) {
            reportTypeComboBox.setValue(reportTypeComboBox.getItems().get(0));
        }
    }
    /**
     * Initializes the month and year filters.
     */
    private void initDateFilters() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        monthComboBox.getItems().clear();
        yearComboBox.getItems().clear();

        for (int month = 1; month <= 12; month++) {
            monthComboBox.getItems().add(month);
        }

        for (int year = currentYear - 5; year <= currentYear + 5; year++) {
            yearComboBox.getItems().add(year);
        }

        monthComboBox.setValue(currentMonth);
        yearComboBox.setValue(currentYear);
    }
    /**
     * Requests the selected report.
     */
    @FXML
    private void handleShowReport() {
        if (clientController == null) {
            statusLabel.setText("Status: Client is not connected");
            return;
        }

        Park selectedPark = parkComboBox.getValue();

        if (selectedPark == null) {
            statusLabel.setText("Status: Please select a park");
            return;
        }

        String reportType = reportTypeComboBox.getValue();
        Integer month = monthComboBox.getValue();
        Integer year = yearComboBox.getValue();

        if (reportType == null || month == null || year == null) {
            statusLabel.setText("Status: Please select all report parameters");
            return;
        }

        int employeeId = getEmployeeIdForRequest();

        if (employeeId <= 0) {
            statusLabel.setText("Status: Employee is not logged in");
            return;
        }

        ReportRequest request = new ReportRequest(
                reportType,
                selectedPark.getParkId(),
                month,
                year,
                employeeId
        );

        clearReportView();

        statusLabel.setText("Status: Loading report...");
        clientController.requestReport(request);
    }
    /**
     * Returns the employee ID used for report requests.
     *
     * @return the employee ID, or -1 if no employee is logged in
     */
    private int getEmployeeIdForRequest() {
        if (!ClientSession.isEmployeeLoggedIn()) {
            return -1;
        }

        return ClientSession.getEmployeeId();
    }
    /**
     * Handles the list of available parks.
     *
     * @param parks the parks returned by the server
     */
    @Override
    public void onParksReceived(List<Park> parks) {
        if (parks == null || parks.isEmpty()) {
            statusLabel.setText("Status: No parks found");
            return;
        }

        ObservableList<Park> visibleParks = FXCollections.observableArrayList();

        if ("park_manager".equals(ClientSession.getEmployeeRole())) {
            int employeeParkId = ClientSession.getEmployeeParkId();

            for (Park park : parks) {
                if (park.getParkId() == employeeParkId) {
                    visibleParks.add(park);
                }
            }
        } else {
            visibleParks.addAll(parks);
        }

        if (visibleParks.isEmpty()) {
            statusLabel.setText("Status: No parks available for this employee");
            return;
        }

        parkComboBox.setItems(visibleParks);
        parkComboBox.setValue(visibleParks.get(0));
    }
    /**
     * Handles the report response from the server.
     *
     * @param response the report response
     */
    @Override
    public void onReportResponse(OperationResponse response) {
        if (response == null) {
            statusLabel.setText("Status: No response from server");
            return;
        }

        if (!response.isSuccess()) {
            clearReportView();
            statusLabel.setText("Status: " + response.getMessage());
            return;
        }

        Object data = response.getData();

        if (!(data instanceof List<?> rows)) {
            clearReportView();
            statusLabel.setText("Status: Invalid report data");
            return;
        }

        String selectedReport = reportTypeComboBox.getValue();

        if (VISITOR_REPORT.equals(selectedReport)) {
            showVisitorReport(rows);
        } else if (CANCELLATION_REPORT.equals(selectedReport)) {
            showCancellationReport(rows);
        } else if (VISIT_DURATION_REPORT.equals(selectedReport)) {
            showVisitDurationReport(rows);
        } else if (PARK_USAGE_REPORT.equals(selectedReport)) {
            showParkUsageReport(rows);
        } else {
            clearReportView();
            statusLabel.setText("Status: Unknown report type");
        }
    }

    // -------------------------------------------------------------------------
    // Visitor report
    // -------------------------------------------------------------------------
    /**
     * Displays the visitor report.
     * @param rows the rows of the report
     * 
     */
    private void showVisitorReport(List<?> rows) {
        setupVisitorReportColumns();
        setTableItems(rows);
        showVisitorPieChart(rows);

        statusLabel.setText("Status: Visitor report loaded");
    }
    /** Configures the visitor report table columns. */
    private void setupVisitorReportColumns() {
        reportsTableView.getColumns().clear();

        reportsTableView.getColumns().add(createColumn("Park", "parkName"));
        reportsTableView.getColumns().add(createColumn("Visitor Type", "visitorType"));
        reportsTableView.getColumns().add(createColumn("Number Of Visits", "numberOfVisits"));
        reportsTableView.getColumns().add(createColumn("Total Visitors", "totalVisitors"));
    }
    /** Displays the visitor report pie chart. 
     * @param rows the rows of the report
     * */
    private void showVisitorPieChart(List<?> rows) {
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        for (Object row : rows) {
            if (row instanceof VisitorReportRow visitorRow) {
                chartData.add(new PieChart.Data(
                        visitorRow.getVisitorType(),
                        visitorRow.getTotalVisitors()
                ));
            }
        }

        reportPieChart.setTitle("Visitors By Type");
        reportPieChart.setData(chartData);

        showPieChart();
    }

    // -------------------------------------------------------------------------
    // Cancellation report
    // -------------------------------------------------------------------------
    /** Displays the cancellation report. 
     * * @param rows the rows of the report
     */
    private void showCancellationReport(List<?> rows) {
        setupCancellationReportColumns();
        setTableItems(rows);
        showCancellationBarChart(rows);

        statusLabel.setText("Status: Cancellation report loaded");
    }
    /** Configures the cancellation report table columns. */
    private void setupCancellationReportColumns() {
        reportsTableView.getColumns().clear();

        reportsTableView.getColumns().add(createColumn("Park", "parkName"));
        reportsTableView.getColumns().add(createColumn("Status", "status"));
        reportsTableView.getColumns().add(createColumn("Total Cancellations", "totalCancellations"));
        reportsTableView.getColumns().add(createColumn("Avg Days Before Visit", "averageDaysBeforeVisit"));
    }
    /** Displays the cancellation report bar chart. 
     * @param rows the rows of the report
     * */
    private void showCancellationBarChart(List<?> rows) {
        Map<String, Integer> totalByStatus = new LinkedHashMap<>();

        for (Object row : rows) {
            if (row instanceof CancellationReportRow cancellationRow) {
                totalByStatus.merge(
                        cancellationRow.getStatus(),
                        cancellationRow.getTotalCancellations(),
                        Integer::sum
                );
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cancellations");

        for (Map.Entry<String, Integer> entry : totalByStatus.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        barChartXAxis.setLabel("Cancellation Status");
        barChartYAxis.setLabel("Total Cancellations");
        barChartXAxis.setTickLabelRotation(0);

        reportBarChart.setTitle("Cancellations By Status");
        reportBarChart.setLegendVisible(false);
        reportBarChart.setAnimated(false);
        reportBarChart.setCategoryGap(45);
        reportBarChart.setBarGap(8);
        reportBarChart.getData().setAll(series);

        showBarChart();
    }

    // -------------------------------------------------------------------------
    // Visit duration report
    // -------------------------------------------------------------------------
    /** Displays the visit duration report. 
     * @param rows the rows of the report
     * */
    private void showVisitDurationReport(List<?> rows) {
        setupVisitDurationReportColumns();
        setTableItems(rows);
        showVisitDurationBarChart(rows);

        statusLabel.setText("Status: Visit duration report loaded");
    }
    /** Configures the visit duration report table columns. 
     * @param rows the rows of the report
     * */
    private void setupVisitDurationReportColumns() {
        reportsTableView.getColumns().clear();

        reportsTableView.getColumns().add(createColumn("Park", "parkName"));
        reportsTableView.getColumns().add(createColumn("Visitor Type", "visitorType"));
        reportsTableView.getColumns().add(createColumn("Number Of Visits", "numberOfVisits"));
        reportsTableView.getColumns().add(createColumn("Avg Duration Minutes", "averageDurationMinutes"));
    }
    /** Displays the visit duration report bar chart. 
     * @param rows the rows of the report
     * */
    private void showVisitDurationBarChart(List<?> rows) {
        ObservableList<XYChart.Series<String, Number>> seriesList =
                FXCollections.observableArrayList();

        for (Object row : rows) {
            if (row instanceof VisitDurationReportRow durationRow) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(durationRow.getVisitorType());

                series.getData().add(new XYChart.Data<>(
                        "Average Duration",
                        durationRow.getAverageDurationMinutes()
                ));

                seriesList.add(series);
            }
        }

        barChartXAxis.setLabel("Metric");
        barChartYAxis.setLabel("Average Duration In Minutes");
        barChartXAxis.setTickLabelRotation(0);

        reportBarChart.setTitle("Average Visit Duration By Visitor Type");
        reportBarChart.setLegendVisible(true);
        reportBarChart.setAnimated(false);
        reportBarChart.setCategoryGap(40);
        reportBarChart.setBarGap(6);
        reportBarChart.getData().setAll(seriesList);

        showBarChart();
    }

    // -------------------------------------------------------------------------
    // Park usage report
    // -------------------------------------------------------------------------
    /** Displays the park usage report. 
     * @param rows the rows of the report
     * */
    private void showParkUsageReport(List<?> rows) {
        setupParkUsageReportColumns();
        setTableItems(rows);
        showParkUsageBarChart(rows);

        statusLabel.setText("Status: Park usage report loaded");
    }
    /** Configures the park usage report table columns. */
    private void setupParkUsageReportColumns() {
        reportsTableView.getColumns().clear();

        reportsTableView.getColumns().add(createColumn("Park", "parkName"));
        reportsTableView.getColumns().add(createColumn("Number Of Visits", "numberOfVisits"));
        reportsTableView.getColumns().add(createColumn("Avg Occupancy %", "averageOccupancyPercent"));
        reportsTableView.getColumns().add(createColumn("Max Occupancy %", "maxOccupancyPercent"));
    }
    /** Displays the park usage report bar chart. 
     * @param rows the rows of the report
     * */
    private void showParkUsageBarChart(List<?> rows) {
        XYChart.Series<String, Number> averageSeries = new XYChart.Series<>();
        averageSeries.setName("Average Occupancy %");

        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Max Occupancy %");

        for (Object row : rows) {
            if (row instanceof ParkUsageReportRow usageRow) {
                averageSeries.getData().add(new XYChart.Data<>(
                        usageRow.getParkName(),
                        usageRow.getAverageOccupancyPercent()
                ));

                maxSeries.getData().add(new XYChart.Data<>(
                        usageRow.getParkName(),
                        usageRow.getMaxOccupancyPercent()
                ));
            }
        }

        barChartXAxis.setLabel("Park");
        barChartYAxis.setLabel("Occupancy Percent");
        barChartXAxis.setTickLabelRotation(0);

        reportBarChart.setTitle("Park Usage");
        reportBarChart.setLegendVisible(true);
        reportBarChart.setAnimated(false);
        reportBarChart.getData().setAll(averageSeries, maxSeries);

        showBarChart();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    /**
     * Creates a table column.
     *
     * @param title the column title
     * @param propertyName the property name
     * @return the created table column
     */
    private TableColumn<Object, Object> createColumn(String title, String propertyName) {
        TableColumn<Object, Object> column = new TableColumn<>(title);
        column.setPrefWidth(180);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }
    /**
     * Sets the report table items.
     *
     * @param rows the report rows
     */
    private void setTableItems(List<?> rows) {
        ObservableList<Object> items = FXCollections.observableArrayList();

        for (Object row : rows) {
            if (row instanceof VisitorReportRow
                    || row instanceof CancellationReportRow
                    || row instanceof VisitDurationReportRow
                    || row instanceof ParkUsageReportRow) {
                items.add(row);
            }
        }

        reportsTableView.setItems(items);
    }
    /** Clears the report display. */
    private void clearReportView() {
        reportsTableView.getItems().clear();
        reportsTableView.getColumns().clear();

        reportPieChart.getData().clear();
        reportBarChart.getData().clear();

        hideCharts();
    }
    /** Shows the pie chart. */
    private void showPieChart() {
        reportPieChart.setVisible(true);
        reportPieChart.setManaged(true);

        reportBarChart.setVisible(false);
        reportBarChart.setManaged(false);
    }
    /** Shows the bar chart. */
    private void showBarChart() {
        reportPieChart.setVisible(false);
        reportPieChart.setManaged(false);

        reportBarChart.setVisible(true);
        reportBarChart.setManaged(true);
    }
    /** Hides all charts. */
    private void hideCharts() {
        reportPieChart.setVisible(false);
        reportPieChart.setManaged(false);

        reportBarChart.setVisible(false);
        reportBarChart.setManaged(false);
    }
    /**
     * Returns to the appropriate dashboard.
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
    		} else {
    			statusLabel.setText("Status: Cannot return back: unknown employee role.");
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