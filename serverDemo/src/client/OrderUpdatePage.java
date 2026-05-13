package client;

/**
 * Sample Skeleton for 'OrderUpdatePage.fxml' Controller Class
 */

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import common.UpdateMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class OrderUpdatePage {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="OrderUpdatePageCancelButton"
    private Button OrderUpdatePageCancelButton; // Value injected by FXMLLoader

    @FXML // fx:id="OrderUpdatePageDatePicker"
    private DatePicker OrderUpdatePageDatePicker; // Value injected by FXMLLoader

    @FXML // fx:id="OrderUpdatePageSpinner"
    private Spinner<Integer> OrderUpdatePageSpinner; // Value injected by FXMLLoader

    @FXML // fx:id="OrderUpdatePageUpdateButton"
    private Button OrderUpdatePageUpdateButton; // Value injected by FXMLLoader

    @FXML // fx:id="OrderUpdatePageDateCheckBox"
    private CheckBox OrderUpdatePageDateCheckBox; // Value injected by FXMLLoader

    @FXML // fx:id="OrderUpdatePageVisitorsCheckBox"
    private CheckBox OrderUpdatePageVisitorsCheckBox; // Value injected by FXMLLoader
    
    private String ordererId;
    private int orderId, orderNumber;
    private LocalDate originalDate;
    private int originalVisitors;
    private ClientService service;
    private Stage prevStage;
    private OrderTableDisplayPage prevController;

    public void setClientService(ClientService service) {
        this.service = service;
    }
    
    public void loadPrevStage(Stage prevStage) {
        this.prevStage = prevStage;
    }

    public void loadPrevController(OrderTableDisplayPage prevController) {
        this.prevController = prevController;
    }

    public void setOrderData(int orderId, LocalDate orderDate, int numberOfVisitors,
    		int orderNumber, String ordererId) {
        this.orderId = orderId;
        this.originalDate = orderDate;
        this.originalVisitors = numberOfVisitors;
        this.orderNumber = orderNumber;
        this.ordererId = ordererId;

        OrderUpdatePageDatePicker.setValue(orderDate);
        OrderUpdatePageSpinner.getValueFactory().setValue(numberOfVisitors);
    }

    @FXML
    void OrderUpdatePageCancelButtonHandler(ActionEvent event) {
        if (prevController != null) {
            prevController.removeOrderFromUpdateWaitingList(orderId);
        }
        
        returnToOrderTable(OrderUpdatePageCancelButton);
    }

    @FXML
    void OrderUpdatePageUpdateButtonHandler(ActionEvent event) {
    	//need to check if the date wasnt changed then spinner needs to change
        LocalDate dateToSend = null;
        int visitorsToSend = 0;

        if (OrderUpdatePageDateCheckBox.isSelected()) {
            dateToSend = OrderUpdatePageDatePicker.getValue();
        }

        if (OrderUpdatePageVisitorsCheckBox.isSelected()) {
            visitorsToSend = OrderUpdatePageSpinner.getValue();
        }

        if (!OrderUpdatePageDateCheckBox.isSelected() &&
            !OrderUpdatePageVisitorsCheckBox.isSelected()) {
            System.out.println("No fields were selected for update.");
            return;
        }
        
        if(dateToSend != null && originalDate.equals(dateToSend))
        	dateToSend = null;
        if(visitorsToSend > 0 && originalVisitors == visitorsToSend)
        	visitorsToSend = 0;
        
        if(dateToSend == null && visitorsToSend == 0) {
        	System.out.println("No update occured.");
            return;
        }
        
        UpdateMessage um =
                new UpdateMessage(dateToSend, visitorsToSend, 
                		orderId, orderNumber, ordererId);

        service.requestUpdate(um);
        returnToOrderTable(OrderUpdatePageUpdateButton);
    }
    
    void returnToOrderTable(Button btn) {
    	Stage currentStage = (Stage) btn.getScene().getWindow();
        currentStage.close();

        if (prevStage != null) {
            prevStage.show();
        }
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert OrderUpdatePageCancelButton != null : "fx:id=\"OrderUpdatePageCancelButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDatePicker != null : "fx:id=\"OrderUpdatePageDatePicker\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageSpinner != null : "fx:id=\"OrderUpdatePageSpinner\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageUpdateButton != null : "fx:id=\"OrderUpdatePageUpdateButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDateCheckBox != null : "fx:id=\"OrderUpdatePageDateCheckBox\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageVisitorsCheckBox != null : "fx:id=\"OrderUpdatePageVisitorsCheckBox\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";

        OrderUpdatePageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 1));

        OrderUpdatePageDatePicker.setEditable(false);

        OrderUpdatePageDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) return;
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        });
        
        // this handles returning to order table when pressing the red X button
     		Platform.runLater(new Runnable() {
     			@Override
     			public void run() {
     				Stage stage = (Stage) OrderUpdatePageUpdateButton.getScene().getWindow();
     				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
     				    @Override
     				    public void handle(WindowEvent event) {
     				    	OrderUpdatePageCancelButtonHandler(null);
     				    }
     				});
     			}
     		});
    }

}