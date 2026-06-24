package clientGUI;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import clientController.ClientController;
import common.CommonConstants;
import common.UpdateMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
@SuppressWarnings("deprecation")
public class OrderUpdateController {

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
    
    @FXML // fx:id="updateLabel"
    private Label updateLabel; // Value injected by FXMLLoader
    
    private String ordererId;
    private int orderId, orderNumber;
    private LocalDate originalDate;
    private int originalVisitors;
    private ClientController clientController;
    private Stage prevStage;
    private OrderTableDisplayController prevController;
    
    /*
     * setter that sets the ClientController on the UI side
     * 
     * @param controller the ClientController
     */
    public void setClientController(ClientController controller) {
        this.clientController = controller;
    }
    
    /*
     * setter that sets the previous stage, 
     * used for when the update is done to load the order table again
     * 
     * @param prevStage the previous stage
     */
    public void setPrevStage(Stage prevStage) {
        this.prevStage = prevStage;
    }
    /*
     * setter that sets the previous UI page's controller, 
     * used for when the update is done to load the order table again
     * 
     * @param prevController the previous stage's controller
     */
    public void setPrevController(OrderTableDisplayController prevController) {
        this.prevController = prevController;
    }
    /*
     * this method sets up the update page and relevant order data
     * 
     * @param orderId 			the ID of the order
     * @param orderDate 		the date of the order
     * @param numberOfVisitors	the number of visitors of the order
     * @param orderNumber		the number of the order in the order table of the user
     * @param ordererId 		the ID of the user
     */
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
    
    /*
     * this function handles pressing the cancel button
     * 
     * @param event		the event of pressing the cancel button
     */
    @FXML
    void OrderUpdatePageCancelButtonHandler(ActionEvent event) {
        if (prevController != null) {
            prevController.removeOrderFromUpdateWaitingList(orderId);
        }
        
        returnToOrderTable(OrderUpdatePageCancelButton);
    }
    
    /*
     * this function handles pressing the update button
     * 
     * @param event		the event of pressing the update button
     */
    @FXML
    void OrderUpdatePageUpdateButtonHandler(ActionEvent event) {
    	
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
        	updateLabel.setTextFill(Color.RED);
        	updateLabel.setText("No fields were selected for update.");
            return;
        }
        
        // check an actual update occurred
        if(dateToSend != null && originalDate.equals(dateToSend))
        	dateToSend = null;
        if(visitorsToSend > 0 && originalVisitors == visitorsToSend)
        	visitorsToSend = 0;
        
        if(dateToSend == null && visitorsToSend == 0) {
        	updateLabel.setTextFill(Color.RED);
        	updateLabel.setText("No update occurred.");
            return;
        }
        
        UpdateMessage um =
                new UpdateMessage(dateToSend, visitorsToSend, 
                		orderId, orderNumber, ordererId);

        clientController.requestUpdate(um);
        returnToOrderTable(OrderUpdatePageUpdateButton);
    }
    
    /*
     * this function handles changing the scene to order table
     * 
     * @param btn		the button clicked
     */
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
        assert updateLabel != null : "fx:id=\"updateLabel\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        
        // setting up the spinner
        OrderUpdatePageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
        		CommonConstants.MIN_VISITOR_COUNT, CommonConstants.MAX_VISITOR_COUNT, 
        		ConstantsUI.START_VISITORS));
        
        // setting up the date picker
        // grays out all dates before the current date, makes the unselectable
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