package clientController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import clientCommon.ChatIF;
import clientCommon.MakeOrderObserver;
import clientCommon.OrderObserver;
import clientCommon.ParkObserver;
import clientCommon.ReportObserver;
import common.Message;
import common.OperationResponse;
import common.Order;
import common.Park;
import common.Protocol;
import common.ReportRequest;
import common.UpdateMessage;
import javafx.application.Platform;
import clientCommon.ParkParameterObserver;
import common.ParkParameterChangeRequest;

/**
 * Connects the client networking layer with the GUI controllers.
 * 
 * The controller sends requests to the server and notifies GUI observers when
 * responses are received.
 */
public class ClientController implements ChatIF {

    private Client client;
    private String id;
    private boolean userIssuedDisconnect = false;

    private List<OrderObserver> orderObservers = new ArrayList<>();
    private List<ParkObserver> parkObservers = new ArrayList<>();
    private List<MakeOrderObserver> makeOrderObservers = new ArrayList<>();
    private List<ReportObserver> reportObservers = new ArrayList<>();
    private List<ParkParameterObserver> parkParameterObservers = new ArrayList<>();
    
    
    public ClientController(String host, int port, String id) throws IOException {
        client = new Client(host, port, this);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Order observers

    public void addObserver(OrderObserver observer) {
        if (observer != null && !orderObservers.contains(observer)) {
            orderObservers.add(observer);
        }
    }

    public void removeObserver(OrderObserver observer) {
        orderObservers.remove(observer);
    }

    private void notifyOrdersReceived(List<Order> rows) {
        for (OrderObserver observer : orderObservers) {
            observer.onOrdersReceived(rows);
        }
    }

    private void notifyOrderMade(Order order) {
        for (OrderObserver observer : orderObservers) {
            observer.addOrder(order);
        }
    }

    private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
        for (OrderObserver observer : orderObservers) {
            observer.onUpdateResult(success, updateMessage);
        }
    }

    // Make order observers

    public void addMakeOrderObserver(MakeOrderObserver observer) {
        if (observer != null && !makeOrderObservers.contains(observer)) {
            makeOrderObservers.add(observer);
        }
    }

    public void removeMakeOrderObserver(MakeOrderObserver observer) {
        makeOrderObservers.remove(observer);
    }

    private void notifyParkNamesMakeOrderObserver(List<String> parkNames) {
        for (MakeOrderObserver observer : makeOrderObservers) {
            observer.onParkNamesReceived(parkNames);
        }
    }

    private void notifyServerResponseMakeOrderObserver(Message message) {
        for (MakeOrderObserver observer : new ArrayList<>(makeOrderObservers)) {
            observer.onMakeOrderServerResponse(message);
        }
    }

    // Park observers

    public void addParkObserver(ParkObserver observer) {
        if (observer != null && !parkObservers.contains(observer)) {
            parkObservers.add(observer);
        }
    }

    public void removeParkObserver(ParkObserver observer) {
        parkObservers.remove(observer);
    }

    private void notifyParksReceived(List<Park> parks) {
        for (ParkObserver observer : parkObservers) {
            observer.onParksReceived(parks);
        }
    }
    
 // Park parameter observers

    public void addParkParameterObserver(ParkParameterObserver observer) {
        if (observer != null && !parkParameterObservers.contains(observer)) {
            parkParameterObservers.add(observer);
        }
    }

    public void removeParkParameterObserver(ParkParameterObserver observer) {
        parkParameterObservers.remove(observer);
    }

    private void notifyPendingParkParameterRequestsReceived(
            List<ParkParameterChangeRequest> requests) {

        for (ParkParameterObserver observer : parkParameterObservers) {
            observer.onPendingParkParameterRequestsReceived(requests);
        }
    }

    private void notifyParkParameterOperationResponse(
            OperationResponse response, Protocol responseType) {

        for (ParkParameterObserver observer : parkParameterObservers) {
            observer.onParkParameterOperationResponse(response, responseType);
        }
    }

    // Report observers

    public void addReportObserver(ReportObserver observer) {
        if (observer != null && !reportObservers.contains(observer)) {
            reportObservers.add(observer);
        }
    }

    public void removeReportObserver(ReportObserver observer) {
        reportObservers.remove(observer);
    }

    private void notifyReportResponse(OperationResponse response) {
        for (ReportObserver observer : reportObservers) {
            observer.onReportResponse(response);
        }
    }

    // Requests to server

    public void requestOrders() {
        sendMessageToServer(new Message(id, Protocol.RETURN_ORDER));
    }

    public void requestUpdate(UpdateMessage updateMessage) {
        sendMessageToServer(new Message(updateMessage, Protocol.UPDATE_ORDER));
    }

    public void requestActiveParks() {
        sendMessageToServer(new Message(null, Protocol.GET_ACTIVE_PARKS));
    }

    public void requestReport(ReportRequest request) {
        sendMessageToServer(new Message(request, Protocol.GET_REPORT_REQUEST));
    }

    public void sendMessageToServer(Message message) {
        if (message != null) {
            client.handleMessageFromClientUI(message);
        }
    }
    
    public void createParkParameterChangeRequest(int parkId, int employeeId,
            String parameterName, String newValue) {

        Object[] data = new Object[] {
                parkId,
                employeeId,
                parameterName,
                newValue
        };

        sendMessageToServer(new Message(
                data,
                Protocol.CREATE_PARK_PARAMETER_CHANGE_REQUEST
        ));
    }
    
    public void requestPendingParkParameterChangeRequests(int employeeId) {
        sendMessageToServer(new Message(
                employeeId,
                Protocol.GET_PENDING_PARK_PARAMETER_CHANGE_REQUESTS
        ));
    }
    
    public void approveParkParameterChangeRequest(int requestId,
            int employeeId, String reviewNote) {

        Object[] data = new Object[] {
                requestId,
                employeeId,
                reviewNote
        };

        sendMessageToServer(new Message(
                data,
                Protocol.APPROVE_PARK_PARAMETER_CHANGE_REQUEST
        ));
    }
    
    public void rejectParkParameterChangeRequest(int requestId,
            int employeeId, String reviewNote) {

        Object[] data = new Object[] {
                requestId,
                employeeId,
                reviewNote
        };

        sendMessageToServer(new Message(
                data,
                Protocol.REJECT_PARK_PARAMETER_CHANGE_REQUEST
        ));
    }
    
    // Messages from server

    @Override
    public void display(Message message) {
        if (message == null) {
            return;
        }

        Platform.runLater(() -> handleServerMessage(message));
    }
    
    private void handlePendingParkParameterRequestsResponse(Object data, Protocol responseType) {
        if (!(data instanceof OperationResponse response)) {
            OperationResponse invalidResponse = new OperationResponse(
                    false,
                    "Invalid pending requests response from server",
                    null
            );

            notifyParkParameterOperationResponse(invalidResponse, responseType);
            return;
        }

        if (!response.isSuccess()) {
            notifyParkParameterOperationResponse(response, responseType);
            return;
        }

        List<ParkParameterChangeRequest> requests =
                parseParkParameterRequestMessage(response.getData());

        if (requests == null) {
            OperationResponse invalidDataResponse = new OperationResponse(
                    false,
                    "Invalid pending requests data from server",
                    null
            );

            notifyParkParameterOperationResponse(invalidDataResponse, responseType);
            return;
        }

        notifyPendingParkParameterRequestsReceived(requests);
    }
    
    private void handleParkParameterOperationResponse(Object data, Protocol responseType) {
        if (data instanceof OperationResponse response) {
            notifyParkParameterOperationResponse(response, responseType);
            return;
        }

        OperationResponse response = new OperationResponse(
                false,
                "Invalid park parameter response from server",
                null
        );

        notifyParkParameterOperationResponse(response, responseType);
    }

    private void handleServerMessage(Message message) {
        Protocol type = message.getType();

        switch (type) {

        case CLIENT_DISCONNECT_SERVER:
            handleServerIssuedDisconnect();
            break;

        case UPDATE_ORDER_SUCCESS:
            notifyUpdateResult(true, (UpdateMessage) message.getData());
            break;

        case UPDATE_ORDER_FAILURE:
            notifyUpdateResult(false, (UpdateMessage) message.getData());
            break;

        case RETURN_ORDER:
            handleReturnOrderResponse(message.getData());
            break;

        case RETURN_PARK_NAMES_SUCCESS:
            handleParkNamesResponse(message.getData());
            break;

        case RETURN_PARK_NAMES_FAILURE:
            notifyParkNamesMakeOrderObserver(null);
            break;
            
        case PENDING_PARK_PARAMETER_CHANGE_REQUESTS_RESULT:
            handlePendingParkParameterRequestsResponse(message.getData(), type);
            break;

        case PARK_PARAMETER_CHANGE_REQUEST_CREATED:
        case PARK_PARAMETER_CHANGE_REQUEST_APPROVED:
        case PARK_PARAMETER_CHANGE_REQUEST_REJECTED:
        case PARK_PARAMETER_CHANGE_REQUEST_FAILURE:
            handleParkParameterOperationResponse(message.getData(), type);
            break;

        case MAKE_ORDER_SUCCESS:
            notifyOrderMade((Order) message.getData());
            notifyServerResponseMakeOrderObserver(message);
            break;

        case MAKE_ORDER_FAIL_NOT_GUIDE:
        case MAKE_ORDER_FAIL_TIME:
        case MAKE_ORDER_FAIL_NOT_SUBSCRIBED:
        case MAKE_ORDER_FAIL:
            notifyServerResponseMakeOrderObserver(message);
            break;

        case ACTIVE_PARKS_RESULT:
        case PARKS_UPDATED:
            handleParksResponse(message.getData());
            break;

        case GET_REPORT_RESPONSE:
            handleReportResponse(message.getData());
            break;
        
        

        default:
            System.out.println("Error: unknown server response in ClientController: " + type);
            break;
        }
    }

    private void handleReturnOrderResponse(Object data) {
        List<Order> rows = parseOrderMessage(data);

        if (rows != null) {
            notifyOrdersReceived(rows);
        }
    }

    private void handleParkNamesResponse(Object data) {
        List<String> parkNames = parseParkNamesMessage(data);

        if (parkNames != null) {
            notifyParkNamesMakeOrderObserver(parkNames);
        } else {
            notifyParkNamesMakeOrderObserver(null);
        }
    }

    private void handleParksResponse(Object data) {
        List<Park> parks = parseParkMessage(data);

        if (parks != null) {
            notifyParksReceived(parks);
        }
    }

    private void handleReportResponse(Object data) {
        if (data instanceof OperationResponse response) {
            notifyReportResponse(response);
            return;
        }

        OperationResponse response = new OperationResponse(
                false,
                "Invalid report response from server",
                null
        );

        notifyReportResponse(response);
    }

    // Disconnect

    public void disconnectFromServer() {
        userIssuedDisconnect = true;
        sendMessageToServer(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
        client.quit();
    }

    public void handleServerIssuedDisconnect() {
        for (OrderObserver observer : orderObservers) {
            observer.handleExit();
        }
    }

    public boolean isUserIssuedDisconnect() {
        return userIssuedDisconnect;
    }

    public void setUserIssuedDisconnect(boolean userIssuedDisconnect) {
        this.userIssuedDisconnect = userIssuedDisconnect;
    }

    // Parsers
    private List<Park> parseParkMessage(Object data) {
        if (!(data instanceof List<?> rawList)) {
            return null;
        }

        List<Park> parks = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof Park)) {
                return null;
            }

            parks.add((Park) item);
        }

        return parks;
    }

    private List<Order> parseOrderMessage(Object data) {
        if (!(data instanceof List<?> rawList)) {
            return null;
        }

        List<Order> orders = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof Order)) {
                return null;
            }

            orders.add((Order) item);
        }

        return orders;
    }

    private List<String> parseParkNamesMessage(Object data) {
        if (!(data instanceof List<?> rawList)) {
            return null;
        }

        List<String> names = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof String)) {
                return null;
            }

            names.add((String) item);
        }

        return names;
    }
    
    
    private List<ParkParameterChangeRequest> parseParkParameterRequestMessage(Object data) {
        if (!(data instanceof List<?> rawList)) {
            return null;
        }

        List<ParkParameterChangeRequest> requests = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof ParkParameterChangeRequest)) {
                return null;
            }

            requests.add((ParkParameterChangeRequest) item);
        }

        return requests;
    }
    
}