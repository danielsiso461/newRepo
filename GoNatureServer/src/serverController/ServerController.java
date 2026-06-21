package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.CommonConstants;
import common.EntryPriceReceipt;
import common.EntryPriceRequest;
import common.Message;
import common.OperationResponse;
import common.Order;
import common.Park;
import common.ParkParameterChangeRequest;
import common.Protocol;
import common.ReportRequest;
import common.UpdateMessage;
import common.ParkVisitorCounterSnapshot;
import common.ParkVisitorCounterUpdateRequest;

import databaseControllers.BillConnection;
import databaseControllers.DBConnectionPool;
import databaseControllers.EmployeeConnection;
import databaseControllers.GuideConnection;
import databaseControllers.OrderConnection;
import databaseControllers.OrderExceedsParkCapacityCheck;
import databaseControllers.ParkConnection;
import databaseControllers.ParkParameterChangeRequestConnection;
import databaseControllers.ReportConnection;
import databaseControllers.SubscriberConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;

/**
 * This class connects the networking part of the server and the server GUI.
 *
 * It handles client requests, updates the server GUI, manages connected users,
 * and communicates with the database connection classes.
 */
public class ServerController implements ServerAndControllerConnection {

    /*
     * Park parameter names used by park_parameter_change_request.
     */
    private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
    private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
            "places_for_unplanned_visitors";
    private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
            "estimated_visit_duration_hours";
    private static final String PARAMETER_PROMOTIONS = "promotions";

    /*
     * Park parameter request statuses.
     */
    private static final String REQUEST_STATUS_PENDING = "pending";

    /*
     * Report type names.
     */
    private static final String REPORT_TYPE_VISITOR = "Visitor Report";
    private static final String REPORT_TYPE_CANCELLATION = "Cancellation Report";
    private static final String REPORT_TYPE_VISIT_DURATION = "Visit Duration Report";
    private static final String REPORT_TYPE_PARK_USAGE = "Park Usage Report";

    /*
     * General messages.
     */
    private static final String REVIEW_NOTE_EMPTY_TEXT = " With no review note.";
    private static final String REVIEW_NOTE_PREFIX = " With review note: \"";
    private static final String REVIEW_NOTE_SUFFIX = "\"";

    private Server server;
    private Set<User> users = new HashSet<>();

    private OrderConnection oc;
    private ParkConnection pc;
    private SubscriberConnection sc;
    private GuideConnection gc;
    private ParkParameterChangeRequestConnection pcrc;
    private OrderExceedsParkCapacityCheck orderChecker;
    private BillConnection bc;

    /**
     * Added for report requests.
     */
    private ReportConnection rc;

    /**
     * Added for checking report permissions.
     */
    private EmployeeConnection ec;

    private int allTimeUserCount = 1;

    private ClientConnectionTableController serverGUIController;

    /**
     * Creates a ServerController object.
     *
     * The constructor initializes the server, creates the database connection
     * objects, and starts listening for clients.
     *
     * @param serverGUIController the server GUI controller
     */
    public ServerController(ClientConnectionTableController serverGUIController) {
        this.serverGUIController = serverGUIController;

        addLog("Initializing server controller.");

        server = Server.getInstance(CommonConstants.DEFAULT_PORT, this);
        addLog("Server instance created on port " + CommonConstants.DEFAULT_PORT + ".");

        try {
            oc = OrderConnection.getInstance();
            pc = ParkConnection.getInstance();
            pcrc = ParkParameterChangeRequestConnection.getInstance();
            sc = SubscriberConnection.getInstance();
            gc = GuideConnection.getInstance();

            rc = ReportConnection.getInstance();
            ec = EmployeeConnection.getInstance();
            bc = BillConnection.getInstance();

            orderChecker = OrderExceedsParkCapacityCheck.getInstance(pc, oc);

            
            addLog("Order database connection object created.");
            addLog("Park database connection object created.");
            addLog("Park parameter change request database connection object created.");
            addLog("Subscriber database connection object created.");
            addLog("Guide database connection object created.");
            addLog("Report database connection object created.");
            addLog("Employee database connection object created.");
            addLog("Order checker object created.");
            addLog("All database connection objects were created successfully.");
            addLog("Bill database connection object created.");
            
        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to create database connection objects: " + e.getMessage());
        }

        try {
            server.listen();
            addLog("Server started listening for clients.");
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
            addLog("ERROR - Could not listen for clients: " + ex.getMessage());
        }
    }

    /**
     * Adds a message to the server log area in the GUI.
     *
     * @param message the message to add to the server log
     */
    public void addLog(String message) {
        if (serverGUIController != null) {
            serverGUIController.addLog(message);
        }
    }

    /**
     * Presents the server connection details in the GUI.
     *
     * @param hostName the server host name
     * @param ip the server IP address
     */
    @Override
    public void presentServerConnection(String hostName, String ip) {
        if (serverGUIController != null) {
            serverGUIController.setLabels(hostName, ip);
        }

        addLog("Server connection details updated. Host: " + hostName + ", IP: " + ip);
    }

    /**
     * Adds a connected user to the server connected users set.
     *
     * @param u the connected user
     * @return true if the user was added successfully, false otherwise
     */
    @Override
    public boolean addUserOnUserConnected(User u) {
        if (u == null) {
            addLog("Tried to connect a null user.");
            return false;
        }

        addLog("Trying to connect user: " + u);

        boolean userIdNotConnected = users.add(u);

        if (userIdNotConnected) {
            u.setUserNumber(allTimeUserCount++);

            if (serverGUIController != null) {
                serverGUIController.onUserConnected(u);
            }

            addLog("User connected successfully: " + u);
        } else {
            addLog("User is already connected: " + u);
        }

        return userIdNotConnected;
    }

    /**
     * Removes a disconnected user from the server connected users set.
     *
     * @param u the disconnected user
     */
    @Override
    public void removeUserOnUserDisconnected(User u) {
        if (u == null) {
            addLog("Tried to disconnect a null user.");
            return;
        }

        addLog("Disconnecting user: " + u);

        if (u.getUserNumber() != null) {
            u.setStatus(false);

            if (serverGUIController != null) {
                serverGUIController.onUserDisconnected(u);
            }

            addLog("User status updated to disconnected: " + u);
        }

        users.remove(u);
        addLog("User removed from connected users set.");
    }

    /**
     * Notifies all connected clients that park data was updated.
     */
    public void notifyParksUpdated() {
        try {
            addLog("Loading updated active parks before notifying clients.");

            List<Park> parks = pc.getAllActiveParks();

            addLog("Loaded " + parks.size() + " active parks for update notification.");

            server.sendToAllClients(new Message(parks, Protocol.PARKS_UPDATED));

            addLog("PARKS_UPDATED message was sent to all connected clients.");

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to notify clients about parks update: " + e.getMessage());
        }
    }

    /**
     * Handles a request received from a client.
     *
     * @param m the message received from the client
     * @return a response message to send back to the client
     */
    @Override
    public Message handleRequest(Message m) {
        if (m == null) {
            addLog("Received null message from client.");
            return null;
        }

        Protocol type = m.getType();
        addLog("Received request from client. Protocol: " + type);

        switch (type) {

        case CLIENT_DISCONNECT_USER:
            addLog("Client requested disconnect.");
            return m;

        case UPDATE_ORDER:
            return handleUpdateOrder(m);

        case RETURN_ORDER:
            return handleReturnOrder(m);

        case GET_PARK_NAMES:
            return handleGetParkNames(m);

        case MAKE_ORDER:
            return handleMakeOrder(m);

        case GET_ACTIVE_PARKS:
            return handleGetActiveParks();

        case APPROVE_PARK_PARAMETER_CHANGE_REQUEST:
            return handleApproveParkParameterChangeRequest(m);

        case REJECT_PARK_PARAMETER_CHANGE_REQUEST:
            return handleRejectParkParameterChangeRequest(m);

        case SEARCH_SUBSCRIBER_REQUEST:
            return handleSearchSubscriber(m);

        case REGISTER_GUIDE_REQUEST:
            return handleRegisterGuide(m);

        case GET_REPORT_REQUEST:
            return handleGetReport(m);

        case CREATE_PARK_PARAMETER_CHANGE_REQUEST:
            return handleCreateParkParameterChangeRequest(m);

        case GET_PENDING_PARK_PARAMETER_CHANGE_REQUESTS:
            return handleGetPendingParkParameterChangeRequests(m);
        
        case CALCULATE_ENTRY_PRICE_REQUEST:
            return handleCalculateEntryPrice(m);
            
        case GET_PARK_VISITOR_COUNTERS_REQUEST:
            return handleGetParkVisitorCounters(m);

        case UPDATE_PARK_VISITOR_COUNTER_REQUEST:
            return handleUpdateParkVisitorCounter(m);
            
        default:
            System.out.println("Error: client request unknown");
            addLog("ERROR - Unknown client request: " + type);
            return null;
        }
    }
    
    private Message handleGetParkVisitorCounters(Message m) {
        addLog("Client requested park visitor counters.");

        try {
            if (!(m.getData() instanceof Integer employeeId)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid employee id",
                        null
                );

                return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
            }

            List<ParkVisitorCounterSnapshot> counters;

            if (ec.isDepartmentManager(employeeId)) {
                counters = pc.getAllParkVisitorCounters();

            } else if (ec.isParkManager(employeeId)) {
                int employeeParkId = ec.getEmployeeParkId(employeeId);

                ParkVisitorCounterSnapshot counter =
                        pc.getParkVisitorCounter(employeeParkId);

                counters = counter == null
                        ? List.of()
                        : List.of(counter);

            } else {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only park managers and department managers can view park visitor counters",
                        null
                );

                return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
            }

            OperationResponse response = new OperationResponse(
                    true,
                    "Park visitor counters loaded successfully",
                    counters
            );

            return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to load park visitor counters: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Database error while loading park visitor counters",
                    null
            );

            return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to load park visitor counters: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to load park visitor counters",
                    null
            );

            return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
        }
    }
    
    private Message handleUpdateParkVisitorCounter(Message m) {
        addLog("Client requested park visitor counter update.");

        try {
            if (!(m.getData() instanceof ParkVisitorCounterUpdateRequest request)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid park visitor counter update request",
                        null
                );

                return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
            }

            if (!canEmployeeUpdateParkVisitorCounter(
                    request.getEmployeeId(),
                    request.getParkId())) {

                OperationResponse response = new OperationResponse(
                        false,
                        "Employee is not allowed to update this park visitor counter",
                        null
                );

                return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
            }

            boolean updated = pc.updateCurrentVisitors(
                    request.getParkId(),
                    request.getEmployeeId(),
                    request.getActionType(),
                    request.getAmount()
            );

            if (!updated) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Failed to update park visitor counter",
                        null
                );

                return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
            }

            ParkVisitorCounterSnapshot updatedCounter =
                    pc.getParkVisitorCounter(request.getParkId());

            OperationResponse response = new OperationResponse(
                    true,
                    "Park visitor counter updated successfully",
                    updatedCounter
            );

            addLog("Park visitor counter updated successfully. Park ID: "
                    + request.getParkId()
                    + ", action: " + request.getActionType()
                    + ", amount: " + request.getAmount());

            notifyParkVisitorCountersUpdated();

            return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to update park visitor counter: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    e.getMessage(),
                    null
            );

            return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to update park visitor counter: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to update park visitor counter",
                    null
            );

            return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
        }
    }
    
    private boolean canEmployeeUpdateParkVisitorCounter(int employeeId, int parkId)
            throws SQLException {

        if (ec.isDepartmentManager(employeeId)) {
            return true;
        }

        if (ec.isParkManager(employeeId) || ec.isParkWorker(employeeId)) {
            int employeeParkId = ec.getEmployeeParkId(employeeId);

            return employeeParkId == parkId;
        }

        return false;
    }
    
    
    private void notifyParkVisitorCountersUpdated() {
        try {
            server.sendToAllClients(
                    new Message(null, Protocol.PARK_VISITOR_COUNTERS_UPDATED)
            );

            addLog("PARK_VISITOR_COUNTERS_UPDATED message was sent to all connected clients.");

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to notify clients about visitor counter update: "
                    + e.getMessage());
        }
    }

    /**
     * Handles creating a park parameter change request.
     *
     * @param m the client message
     * @return success or failure response
     */
    private Message handleCreateParkParameterChangeRequest(Message m) {
        addLog("Client requested to create park parameter change request.");

        try {
            if (!(m.getData() instanceof Object[] data)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid park parameter change request data",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            int parkId = (int) data[0];
            int requestedByEmployeeId = (int) data[1];
            String parameterName = data[2] == null ? null : data[2].toString().trim();
            String newValue = data[3] == null ? null : data[3].toString().trim();

            if (!ec.isParkManager(requestedByEmployeeId)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only park managers can request park parameter changes",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            int employeeParkId = ec.getEmployeeParkId(requestedByEmployeeId);

            if (employeeParkId != parkId) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Park manager can request changes only for his own park",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            Park park = pc.getFullParkById(parkId);

            if (park == null) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Park was not found",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            String oldValue = getCurrentParkParameterValue(park, parameterName);

            if (oldValue == null) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Unknown park parameter",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            boolean created = pcrc.createChangeRequest(
                    parkId,
                    requestedByEmployeeId,
                    parameterName,
                    oldValue,
                    newValue
            );

            if (!created) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Failed to create park parameter change request",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            OperationResponse response = new OperationResponse(
                    true,
                    "Park parameter change request was created successfully",
                    null
            );

            addLog("Park parameter change request was created successfully. Parameter: "
                    + parameterName + ", old value: " + oldValue + ", new value: " + newValue);

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_CREATED);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to create park parameter change request: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Database error while creating park parameter change request",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Invalid park parameter change request data: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to create park parameter change request",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
        }
    }

    /**
     * Returns the current value of a park parameter before creating a change request.
     *
     * @param park the park
     * @param parameterName the requested parameter name
     * @return the current parameter value as String
     */
    private String getCurrentParkParameterValue(Park park, String parameterName) {
        if (park == null || parameterName == null) {
            return null;
        }

        switch (parameterName) {

        case PARAMETER_MAX_CAPACITY:
            return String.valueOf(park.getMaxCapacity());

        case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
            return String.valueOf(park.getPlacesForUnplannedVisitors());

        case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
            return String.valueOf((int) park.getEstimatedVisitDurationHours());

        case PARAMETER_PROMOTIONS:
            return String.valueOf(park.getPromotions());

        default:
            return null;
        }
    }

    /**
     * Handles loading pending park parameter change requests.
     *
     * @param m the client message
     * @return pending requests response
     */
    private Message handleGetPendingParkParameterChangeRequests(Message m) {
        addLog("Client requested pending park parameter change requests.");

        try {
            if (!(m.getData() instanceof Integer employeeId)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid employee id",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            if (!ec.isDepartmentManager(employeeId)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only department managers can view pending parameter change requests",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            List<ParkParameterChangeRequest> requests = pcrc.getPendingRequests();

            OperationResponse response = new OperationResponse(
                    true,
                    "Pending park parameter change requests loaded successfully",
                    requests
            );

            return new Message(response, Protocol.PENDING_PARK_PARAMETER_CHANGE_REQUESTS_RESULT);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to load pending park parameter change requests: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Database error while loading pending requests",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to load pending park parameter change requests: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to load pending requests",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
        }
    }

    /**
     * Handles a report request from a client.
     *
     * The method checks:
     * 1. The request object is valid.
     * 2. The requested month, year, and park are valid.
     * 3. The employee is allowed to view the requested park report.
     * 4. The requested report type exists.
     *
     * @param m the client message
     * @return report response message
     */
    private Message handleGetReport(Message m) {
        addLog("Client requested report.");

        try {
            if (!(m.getData() instanceof ReportRequest)) {
                addLog("Invalid report request data.");

                OperationResponse response =
                        new OperationResponse(false, "Invalid report request", null);

                return new Message(response, Protocol.GET_REPORT_RESPONSE);
            }

            ReportRequest request = (ReportRequest) m.getData();

            if (!isReportRequestValid(request)) {
                OperationResponse response =
                        new OperationResponse(false, "Invalid report parameters", null);

                return new Message(response, Protocol.GET_REPORT_RESPONSE);
            }

            if (!isEmployeeAllowedToViewReport(request)) {
                addLog("Employee is not allowed to view this report. Employee ID: "
                        + request.getEmployeeId() + ", Park ID: " + request.getParkId());

                OperationResponse response =
                        new OperationResponse(false,
                                "You are not allowed to view this park report",
                                null);

                return new Message(response, Protocol.GET_REPORT_RESPONSE);
            }

            Object reportData;

            switch (request.getReportType()) {

            case REPORT_TYPE_VISITOR:
                addLog("Loading visitor report. Park ID: " + request.getParkId()
                        + ", Month: " + request.getMonth()
                        + ", Year: " + request.getYear());

                reportData = rc.getVisitorReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;

            case REPORT_TYPE_CANCELLATION:
                addLog("Loading cancellation report. Park ID: " + request.getParkId()
                        + ", Month: " + request.getMonth()
                        + ", Year: " + request.getYear());

                reportData = rc.getCancellationReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;

            case REPORT_TYPE_VISIT_DURATION:
                reportData = rc.getVisitDurationReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;

            case REPORT_TYPE_PARK_USAGE:
                reportData = rc.getParkUsageReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;

            default:
                addLog("Unknown report type: " + request.getReportType());

                OperationResponse response =
                        new OperationResponse(false, "Unknown report type", null);

                return new Message(response, Protocol.GET_REPORT_RESPONSE);
            }

            OperationResponse response =
                    new OperationResponse(true, "Report loaded successfully", reportData);

            addLog("Report loaded successfully.");

            return new Message(response, Protocol.GET_REPORT_RESPONSE);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Database error while loading report: " + e.getMessage());

            OperationResponse response =
                    new OperationResponse(false, "Database error while loading report", null);

            return new Message(response, Protocol.GET_REPORT_RESPONSE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to handle report request: " + e.getMessage());

            OperationResponse response =
                    new OperationResponse(false, "Failed to load report", null);

            return new Message(response, Protocol.GET_REPORT_RESPONSE);
        }
    }

    /**
     * Checks if the report request basic parameters are valid.
     *
     * @param request the report request
     * @return true if valid, false otherwise
     * @throws SQLException if checking the park fails
     */
    private boolean isReportRequestValid(ReportRequest request) throws SQLException {
        if (request == null) {
            return false;
        }

        if (request.getParkId() <= 0) {
            return false;
        }

        if (request.getMonth() < 1 || request.getMonth() > 12) {
            return false;
        }

        if (request.getYear() < 2000) {
            return false;
        }

        if (request.getEmployeeId() <= 0) {
            return false;
        }

        if (request.getReportType() == null || request.getReportType().isBlank()) {
            return false;
        }

        Park park = pc.getFullParkById(request.getParkId());

        return park != null;
    }

    /**
     * Checks whether an employee is allowed to view the requested report.
     *
     * Department manager can view reports for every park.
     * Park manager can view reports only for the park connected to him.
     *
     * @param request the report request
     * @return true if allowed, false otherwise
     * @throws SQLException if checking employee details fails
     */
    private boolean isEmployeeAllowedToViewReport(ReportRequest request) throws SQLException {
        int employeeId = request.getEmployeeId();
        int requestedParkId = request.getParkId();

        if (ec.isDepartmentManager(employeeId)) {
            return true;
        }

        if (ec.isParkManager(employeeId)) {
            int employeeParkId = ec.getEmployeeParkId(employeeId);

            return employeeParkId == requestedParkId
                    && isReportAllowedForParkManager(request.getReportType());
        }

        return false;
    }

    /**
     * Checks whether a park manager is allowed to view the requested report type.
     *
     * @param reportType the report type
     * @return true if allowed
     */
    private boolean isReportAllowedForParkManager(String reportType) {
        return REPORT_TYPE_VISITOR.equals(reportType)
                || REPORT_TYPE_CANCELLATION.equals(reportType);
    }

    /**
     * Handles an order update request.
     *
     * @param m the client message
     * @return update success or failure message
     */
    private Message handleUpdateOrder(Message m) {
        addLog("Client requested order update.");

        Protocol typeRet = Protocol.UPDATE_ORDER_SUCCESS;
        UpdateMessage um = (UpdateMessage) m.getData();

        try {
            oc.updateOrder(um);
            addLog("Order updated successfully: " + um);
        } catch (SQLException e) {
            typeRet = Protocol.UPDATE_ORDER_FAILURE;
            System.out.println(e.getMessage());
            addLog("ERROR - Failed to update order: " + e.getMessage());
        }

        return new Message(m.getData(), typeRet);
    }

    /**
     * Handles a request for returning user orders.
     *
     * @param m the client message
     * @return a message containing the user's orders, or null if loading failed
     */
    private Message handleReturnOrder(Message m) {
        addLog("Client requested orders list.");

        List<Order> orders = null;

        try {
            orders = oc.getUserOrders(m);
            addLog("Orders were loaded from database.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            addLog("ERROR - Failed to load orders: " + e.getMessage());
        }

        if (orders != null) {
            addLog("Returning " + orders.size() + " orders to client.");
            return new Message(orders, Protocol.RETURN_ORDER);
        }

        addLog("No orders returned to client.");
        return null;
    }

    /**
     * Handles a request for getting park names.
     *
     * @param m the client message
     * @return a message containing a list of park names
     */
    private Message handleGetParkNames(Message m) {
        addLog("Client requested active parks name list.");

        try {
            addLog("Loading names of active parks from database.");
            addLog("Returning active parks name list to client.");

            List<String> parkNames = pc.getActiveParksNames();

            if (parkNames.isEmpty()) {
                addLog("Error - no park names fetched.");
                return new Message(null, Protocol.RETURN_PARK_NAMES_FAILURE);
            }

            addLog("Active parks name list loaded from database.");
            return new Message(parkNames, Protocol.RETURN_PARK_NAMES_SUCCESS);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to load active parks: " + e.getMessage());
            return new Message(null, Protocol.RETURN_PARK_NAMES_FAILURE);
        }
    }

    /**
     * Handles a request for making order.
     *
     * @param m the client message
     * @return a message containing the user order on success, or a fail message
     */
    private Message handleMakeOrder(Message m) {
        if (!(m.getData() instanceof Order)) {
            addLog("Make Order Request Unapproved - unknown error.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        Order o = (Order) m.getData();

        o.setPlacementDate(LocalDate.now());

        int parkId = -1;

        try {
            addLog("Getting parkId from the park name from the DB.");
            parkId = pc.getParkIdByName(o.getParkName());
        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to execute query to get parkId from park name.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        if (parkId == -1) {
            addLog("Make Order Request Unapproved - park name unknown.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        addLog("Successfully fetched parkId from the DB.");
        o.setParkId(parkId);

        if (o.getOrderType().equals(Order.ORDER_TYPE_ORGANIZED)) {
            Integer guideId = null;

            try {
                guideId = gc.isActiveGuide(o.getUserId());
            } catch (SQLException e) {
                e.printStackTrace();
                addLog("ERROR - Failed to execute query to check if user is a guide.");
                return new Message(null, Protocol.MAKE_ORDER_FAIL);
            }

            if (guideId == null) {
                addLog("Make Order Request Unapproved - user is not a guide.");
                return new Message(null, Protocol.MAKE_ORDER_FAIL_NOT_GUIDE);
            }

            o.setGuideId(guideId);

        } else if (o.getGuideId() != null) {
            o.setGuideId(null);
        }

        addLog("Successfully handled guide checking.");

        boolean isSubscribed = false;

        try {
            isSubscribed = sc.subscriberExists(o.getUserId());

            if (isSubscribed) {
                o.setIsSubscribedToTrue();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to execute query to check if user is subscribed.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        if (o.getVisitorNumber() > 1) {
            if (!isSubscribed) {
                addLog("Make Order Request Unapproved - user is not subscribed.");
                return new Message(null, Protocol.MAKE_ORDER_FAIL_NOT_SUBSCRIBED);
            }
        }

        addLog("Successfully handled subscriber checking.");

        if (!checkOrderDetailsAreValid(o)) {
            addLog("Make Order Request Unapproved - order details are invalid.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        int check = -1;

        try {
            check = orderChecker.check(o);
        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to execute query to check if order can be booked.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        if (check == -1) {
            addLog("Make Order Request Unapproved - bad order.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        } else if (check == 1) {
            addLog("Make Order Request Unapproved - bad order time.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL_TIME);
        }

        addLog("Successfully handled all checks.");

        o.setOrderStatus("approved");

        try {
            oc.bookOrder(o);
        } catch (SQLException e) {
            o.setOrderStatus(REQUEST_STATUS_PENDING);
            addLog("Make Order Request Failed - SQL error.");
            e.printStackTrace();
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        } catch (Exception e) {
            o.setOrderStatus(REQUEST_STATUS_PENDING);
            addLog("Make Order Request Failed - resource allocation error.");
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        }

        String pn = null;

        if (isSubscribed) {
            try {
                pn = sc.getPhoneNumberById(o.getUserId());
            } catch (Exception e) {
                e.printStackTrace();
                addLog("ERROR - Failed to execute query to get order phone number.");
                return new Message(null, Protocol.MAKE_ORDER_FAIL);
            }

            if (pn == null) {
                addLog("ERROR - phone number doesn't exist for existing subscriber.");
                return new Message(null, Protocol.MAKE_ORDER_FAIL);
            }
        }

        o.setPhoneNumber(pn);
        addLog("Make Order request successful - " + o.getOrderId());

        return new Message(o, Protocol.MAKE_ORDER_SUCCESS);
    }

    /**
     * Handles a request for all active parks.
     *
     * @return a message containing active parks, or a failure message
     */
    private Message handleGetActiveParks() {
        addLog("Client requested active parks list.");

        try {
            addLog("Loading active parks from database.");

            List<Park> parks = pc.getAllActiveParks();

            addLog("Active parks list loaded from database. Number of parks: " + parks.size());
            addLog("Returning active parks list to client.");

            return new Message(parks, Protocol.ACTIVE_PARKS_RESULT);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to load active parks: " + e.getMessage());

            return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
        }
    }

    /**
     * Handles a client request to search for a subscriber by subscriber ID.
     *
     * @param m the message received from the client
     * @return a message with the search result
     */
    private Message handleSearchSubscriber(Message m) {
        return null;
    }

    /**
     * Handles a client request to register an existing subscriber as a guide.
     *
     * @param m the message received from the client
     * @return a message with the registration result
     */
    private Message handleRegisterGuide(Message m) {
        return null;
    }
    
    private Message handleCalculateEntryPrice(Message m) {
        addLog("Client requested entry price calculation.");

        try {
            if (!(m.getData() instanceof EntryPriceRequest request)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid entry price request",
                        null
                );

                return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);
            }

            EntryPriceReceipt receipt =
                    bc.calculateReceiptByOrderNumber(request.getOrderNumber());

            OperationResponse response = new OperationResponse(
                    true,
                    "Entry price calculated successfully",
                    receipt
            );

            addLog("Entry price calculated successfully for order number: "
                    + request.getOrderNumber());

            return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to calculate entry price: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    e.getMessage(),
                    null
            );

            return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Failed to calculate entry price: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to calculate entry price",
                    null
            );

            return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);
        }
    }

    /**
     * Handles approval of a park parameter change request.
     *
     * @param m the client message
     * @return approval success or failure message
     */
    private Message handleApproveParkParameterChangeRequest(Message m) {
        addLog("Client requested approval of park parameter change request.");

        try {
            if (!(m.getData() instanceof Object[] data)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid approval request data",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            int requestId = (int) data[0];
            int approvedByEmployeeId = (int) data[1];
            String reviewNote = data.length > 2 && data[2] != null
                    ? data[2].toString().trim()
                    : "";

            if (!ec.isDepartmentManager(approvedByEmployeeId)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only department managers can approve parameter change requests",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            ParkParameterChangeRequest request = pcrc.getRequestById(requestId);

            if (request == null) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Request was not found",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            if (!REQUEST_STATUS_PENDING.equals(request.getRequestStatus())) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only pending requests can be approved",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            boolean parkUpdated = pc.updateParkParameter(
                    request.getParkId(),
                    request.getParameterName(),
                    request.getNewValue()
            );

            if (!parkUpdated) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Failed to update park parameter",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            boolean approved = pcrc.approveRequest(
                    requestId,
                    approvedByEmployeeId,
                    reviewNote
            );

            if (!approved) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Failed to approve park parameter change request",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            notifyParksUpdated();

            String message = buildReviewNoteMessage(
                    "Park parameter change request approved successfully.",
                    reviewNote
            );

            addLog(message + " Request ID: " + requestId);

            OperationResponse response = new OperationResponse(
                    true,
                    message,
                    requestId
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to approve park parameter change request: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Database error while approving request",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Invalid approval request data: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to approve request",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
        }
    }

    /**
     * Handles rejection of a park parameter change request.
     *
     * @param m the client message
     * @return rejection success or failure message
     */
    private Message handleRejectParkParameterChangeRequest(Message m) {
        addLog("Client requested rejection of park parameter change request.");

        try {
            if (!(m.getData() instanceof Object[] data)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Invalid rejection request data",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            int requestId = (int) data[0];
            int approvedByEmployeeId = (int) data[1];
            String reviewNote = data.length > 2 && data[2] != null
                    ? data[2].toString().trim()
                    : "";

            if (!ec.isDepartmentManager(approvedByEmployeeId)) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only department managers can reject parameter change requests",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            ParkParameterChangeRequest request = pcrc.getRequestById(requestId);

            if (request == null) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Request was not found",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            if (!REQUEST_STATUS_PENDING.equals(request.getRequestStatus())) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Only pending requests can be rejected",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            boolean rejected = pcrc.rejectRequest(
                    requestId,
                    approvedByEmployeeId,
                    reviewNote
            );

            if (!rejected) {
                OperationResponse response = new OperationResponse(
                        false,
                        "Failed to reject park parameter change request",
                        null
                );

                return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
            }

            String message = buildReviewNoteMessage(
                    "Park parameter change request rejected successfully.",
                    reviewNote
            );

            addLog(message + " Request ID: " + requestId);

            OperationResponse response = new OperationResponse(
                    true,
                    message,
                    requestId
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to reject park parameter change request: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Database error while rejecting request",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Invalid rejection request data: " + e.getMessage());

            OperationResponse response = new OperationResponse(
                    false,
                    "Failed to reject request",
                    null
            );

            return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
        }
    }

    /**
     * Checks if the details of a given order are valid.
     *
     * @param o the order to check
     * @return true if valid, false otherwise
     */
    private boolean checkOrderDetailsAreValid(Order o) {
        if (o.getOrderDate().isBefore(LocalDate.now())) {
            return false;
        }

        if (o.getVisitorNumber() > CommonConstants.MAX_VISITOR_COUNT
                || o.getVisitorNumber() < CommonConstants.MIN_VISITOR_COUNT) {
            return false;
        }

        if (o.getOrderHour() > CommonConstants.MAX_HOUR
                || o.getOrderHour() < CommonConstants.MIN_HOUR) {
            return false;
        }

        if (!Order.ORDER_STATUS_PENDING.equals(o.getOrderStatus())) {
            return false;
        }

        return true;
    }

    /**
     * Closes all database connections safely.
     */
    private void closeDBConnection() {
        try {
            addLog("Closing database connections.");

            if (oc != null) {
                oc.close();
                addLog("Order database connection returned to pool.");
            }

            if (pc != null) {
                pc.close();
                addLog("Park database connection returned to pool.");
            }

            if (pcrc != null) {
                pcrc.close();
                addLog("Park parameter change request database connection returned to pool.");
            }

            if (sc != null) {
                sc.close();
                addLog("Subscriber database connection returned to pool.");
            }

            if (gc != null) {
                gc.close();
                addLog("Guide database connection returned to pool.");
            }

            if (rc != null) {
                rc.close();
                addLog("Report database connection returned to pool.");
            }

            if (ec != null) {
                ec.close();
                addLog("Employee database connection returned to pool.");
            }

            if (bc != null) {
                bc.close();
                addLog("Bill database connection returned to pool.");
            }
            
            DBConnectionPool.getInstance().closeAllConnections();
            addLog("All database pool connections were closed.");
            addLog("Database connections closed successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to close database connection: " + e.getMessage());
        }
    }

    /**
     * Closes the server safely.
     */
    @Override
    public void closeServer() {
        try {
            addLog("Closing server.");

            server.sendToAllClients(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
            addLog("Disconnect message sent to all clients.");

            server.stopListening();
            addLog("Server stopped listening.");

            server.close();
            addLog("Server closed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to close server: " + e.getMessage());
        } finally {
            closeDBConnection();
        }
    }

    /**
     * Builds a success message that includes the review note.
     *
     * @param baseMessage the original success message
     * @param reviewNote the department manager review note
     * @return message with review note
     */
    private String buildReviewNoteMessage(String baseMessage, String reviewNote) {
        if (reviewNote == null || reviewNote.isBlank()) {
            return baseMessage + REVIEW_NOTE_EMPTY_TEXT;
        }

        return baseMessage + REVIEW_NOTE_PREFIX + reviewNote.trim() + REVIEW_NOTE_SUFFIX;
    }
}