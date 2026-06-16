package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.CommonConstants;
import common.GuideRegistrationRequest;
import common.Message;
import common.OperationResponse;
import common.Order;
import common.Park;
import common.Protocol;
import common.ReportRequest;
import common.UpdateMessage;

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

    private Server server;
    private Set<User> users = new HashSet<>();

    private OrderConnection oc;
    private ParkConnection pc;
    private SubscriberConnection sc;
    private GuideConnection gc;
    private ParkParameterChangeRequestConnection pcrc;
    private OrderExceedsParkCapacityCheck orderChecker;

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

        server = Server.getInstance(common.CommonConstants.DEFAULT_PORT, this);
        addLog("Server instance created on port " + common.CommonConstants.DEFAULT_PORT + ".");

        try {
            oc = OrderConnection.getInstance();
            pc = ParkConnection.getInstance();
            pcrc = ParkParameterChangeRequestConnection.getInstance();
            sc = SubscriberConnection.getInstance();
            gc = GuideConnection.getInstance();

            /**
             * Added for reports.
             */
            rc = ReportConnection.getInstance();
            ec = EmployeeConnection.getInstance();

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

        /**
         * Added for reports.
         */
        case GET_REPORT_REQUEST:
            return handleGetReport(m);

        default:
            System.out.println("Error: client request unknown");
            addLog("ERROR - Unknown client request: " + type);
            return null;
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

            case "Visitor Report":
                addLog("Loading visitor report. Park ID: " + request.getParkId()
                        + ", Month: " + request.getMonth()
                        + ", Year: " + request.getYear());

                reportData = rc.getVisitorReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;

            case "Cancellation Report":
                addLog("Loading cancellation report. Park ID: " + request.getParkId()
                        + ", Month: " + request.getMonth()
                        + ", Year: " + request.getYear());

                reportData = rc.getCancellationReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;
                
            case "Visit Duration Report":
                reportData = rc.getVisitDurationReport(
                        request.getParkId(),
                        request.getMonth(),
                        request.getYear()
                );
                break;

            case "Park Usage Report":
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
    
    private boolean isReportAllowedForParkManager(String reportType) {
        return "Visitor Report".equals(reportType)
                || "Cancellation Report".equals(reportType);
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
            o.setOrderStatus("pending");
            addLog("Make Order Request Failed - SQL error.");
            e.printStackTrace();
            return new Message(null, Protocol.MAKE_ORDER_FAIL);
        } catch (Exception e) {
            o.setOrderStatus("pending");
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

    /**
     * Handles approval of a park parameter change request.
     *
     * @param m the client message
     * @return approval success or failure message
     */
    private Message handleApproveParkParameterChangeRequest(Message m) {
        addLog("Client requested approval of park parameter change request.");

        try {
            Object[] data = (Object[]) m.getData();

            int requestId = (int) data[0];
            int approvedByEmployeeId = (int) data[1];
            String reviewNote = (String) data[2];

            addLog("Approving park parameter change request. Request ID: " + requestId
                    + ", approved by employee ID: " + approvedByEmployeeId);

            boolean approved = pcrc.approveRequest(requestId, approvedByEmployeeId, reviewNote);

            if (approved) {
                addLog("Park parameter change request approved successfully. Request ID: " + requestId);
                addLog("Park data may have changed. Notifying all clients.");

                notifyParksUpdated();

                return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED);
            }

            addLog("Park parameter change request approval failed. Request ID: " + requestId);
            return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to approve park parameter change request: " + e.getMessage());
            return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Invalid approval request data: " + e.getMessage());
            return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
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
            Object[] data = (Object[]) m.getData();

            int requestId = (int) data[0];
            int approvedByEmployeeId = (int) data[1];
            String reviewNote = (String) data[2];

            addLog("Rejecting park parameter change request. Request ID: " + requestId
                    + ", reviewed by employee ID: " + approvedByEmployeeId);

            boolean rejected = pcrc.rejectRequest(requestId, approvedByEmployeeId, reviewNote);

            if (rejected) {
                addLog("Park parameter change request rejected successfully. Request ID: " + requestId);
                return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED);
            }

            addLog("Park parameter change request rejection failed. Request ID: " + requestId);
            return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (SQLException e) {
            e.printStackTrace();
            addLog("ERROR - Failed to reject park parameter change request: " + e.getMessage());
            return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

        } catch (Exception e) {
            e.printStackTrace();
            addLog("ERROR - Invalid rejection request data: " + e.getMessage());
            return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
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

        if (!(o.getOrderStatus().equals(Order.ORDER_STATUS_PENDING))) {
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
}