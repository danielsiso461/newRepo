
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

/**
 * Controller for the order update page.
 *
 * This screen allows the user to update the order date,
 * the number of visitors, or both.
 */
@SuppressWarnings("deprecation")
public class OrderUpdateController {
 
    /**
     * the resource bundle used by the FXML loader
     */
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    /**
     * the location URL used by the FXML loader
     */
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    /**
     * the cancel button of the order update page
     */
    @FXML // fx:id="OrderUpdatePageCancelButton"
    private Button OrderUpdatePageCancelButton; // Value injected by FXMLLoader

    /**
     * the date picker used to select the updated order date
     */
    @FXML // fx:id="OrderUpdatePageDatePicker"
    private DatePicker OrderUpdatePageDatePicker; // Value injected by FXMLLoader

    /**
     * the spinner used to select the updated number of visitors
     */
    @FXML // fx:id="OrderUpdatePageSpinner"
    private Spinner<Integer> OrderUpdatePageSpinner; // Value injected by FXMLLoader

    /**
     * the update button of the order update page
     */
    @FXML // fx:id="OrderUpdatePageUpdateButton"
    private Button OrderUpdatePageUpdateButton; // Value injected by FXMLLoader

    /**
     * the checkbox used to choose whether to update the order date
     */
    @FXML // fx:id="OrderUpdatePageDateCheckBox"
    private CheckBox OrderUpdatePageDateCheckBox; // Value injected by FXMLLoader

    /**
     * the checkbox used to choose whether to update the number of visitors
     */
    @FXML // fx:id="OrderUpdatePageVisitorsCheckBox"
    private CheckBox OrderUpdatePageVisitorsCheckBox; // Value injected by FXMLLoader
    
    /**
     * the label used to display update messages
     */
    @FXML // fx:id="updateLabel"
    private Label updateLabel; // Value injected by FXMLLoader
    
    /**
     * the ID of the user who made the order
     */
    private String ordererId;

    /**
     * the order ID of the selected order
     */
    private int orderId;
    
    /**
     * the order number of the selected order
     */
    private int orderNumber;
    

    /**
     * the original date of the order
     */
    private LocalDate originalDate;

    /**
     * the original number of visitors in the order
     */
    private int originalVisitors;

    /**
     * the client controller used to communicate with the server
     */
    private ClientController clientController;

    /**
     * the previous stage that should be shown after closing this page
     */
    private Stage prevStage;

    /**
     * the previous controller used to update the order table page
     */
    private OrderTableDisplayController prevController;
    
    /**
     * Sets the ClientController on the UI side.
     * 
     * @param controller the ClientController
     */
    public void setClientController(ClientController controller) {
        this.clientController = controller;
    }
    
    /**
     * Sets the previous stage.
     *
     * This stage is used when the update page is closed
     * and the order table page should be shown again.
     * 
     * @param prevStage the previous stage
     */
    public void setPrevStage(Stage prevStage) {
        this.prevStage = prevStage;
    }

    /**
     * Sets the previous UI page controller.
     *
     * This controller is used when the update page is closed
     * and the order table page should be updated.
     * 
     * @param prevController the previous page controller
     */
    public void setPrevController(OrderTableDisplayController prevController) {
        this.prevController = prevController;
    }

    /**
     * Sets the order data for the update page.
     * 
     * @param orderId the ID of the order
     * @param orderDate the date of the order
     * @param numberOfVisitors the number of visitors of the order
     * @param orderNumber the number of the order in the order table of the user
     * @param ordererId the ID of the user
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
    
    /**
     * Handles the click on the cancel button.
     * 
     * @param event the button click event
     */
    @FXML
    void OrderUpdatePageCancelButtonHandler(ActionEvent event) {
        if (prevController != null) {
            prevController.removeOrderFromUpdateWaitingList(orderId);
        }
        
        returnToOrderTable(OrderUpdatePageCancelButton);
    }
    
    /**
     * Handles the click on the update button.
     *
     * This method checks which fields were selected for update,
     * validates that an actual change was made, and sends the update request.
     * 
     * @param event the button click event
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
    
    /**
     * Returns to the order table page.
     * 
     * @param btn the button that was clicked
     */
    void returnToOrderTable(Button btn) {
    	Stage currentStage = (Stage) btn.getScene().getWindow();
        currentStage.close();

        if (prevStage != null) {
            prevStage.show();
        }
    }
    
    /**
     * Updates the visibility of the update label.
     */
    private void updateUpdateLabelVisibility() {
        boolean hasMessage = updateLabel.getText() != null
                && !updateLabel.getText().isBlank();

        updateLabel.setVisible(hasMessage);
        updateLabel.setManaged(hasMessage);
    }

    /**
     * Initializes the order update page.
     *
     * This method sets the spinner, date picker, label visibility,
     * and close request behavior.
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert OrderUpdatePageCancelButton != null : "fx:id=\"OrderUpdatePageCancelButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDatePicker != null : "fx:id=\"OrderUpdatePageDatePicker\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageSpinner != null : "fx:id=\"OrderUpdatePageSpinner\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageUpdateButton != null : "fx:id=\"OrderUpdatePageUpdateButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDateCheckBox != null : "fx:id=\"OrderUpdatePageDateCheckBox\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageVisitorsCheckBox != null : "fx:id=\"OrderUpdatePageVisitorsCheckBox\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert updateLabel != null : "fx:id=\"updateLabel\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        
        updateLabel.textProperty().addListener((observable, oldText, newText) -> {
            updateUpdateLabelVisibility();
        });

        updateUpdateLabelVisibility();
        
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

