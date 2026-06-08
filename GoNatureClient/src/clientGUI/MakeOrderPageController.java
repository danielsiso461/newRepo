package clientGUI;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import clientCommon.MakeOrderObserver;
import clientController.ClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import common.*;
/**
 * Sample Skeleton for 'makeOrderPage.fxml' Controller Class
 */

import javafx.scene.control.TextField;

public class MakeOrderPageController implements MakeOrderObserver {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="buttonBar"
    private ButtonBar buttonBar; // Value injected by FXMLLoader

    @FXML // fx:id="datePicker"
    private DatePicker datePicker; // Value injected by FXMLLoader

    @FXML // fx:id="email"
    private TextField email; // Value injected by FXMLLoader

    @FXML // fx:id="enterWaitingListButton"
    private Button enterWaitingListButton; // Value injected by FXMLLoader

    @FXML // fx:id="errorLabel"
    private Label errorLabel; // Value injected by FXMLLoader

    @FXML // fx:id="guideCheckbox"
    private CheckBox guideCheckbox; // Value injected by FXMLLoader

    @FXML // fx:id="hourPicker"
    private ComboBox<Integer> hourPicker; // Value injected by FXMLLoader

    @FXML // fx:id="makeOrderButton"
    private Button makeOrderButton; // Value injected by FXMLLoader

    @FXML // fx:id="parkPicker"
    private ComboBox<String> parkPicker; // Value injected by FXMLLoader

    @FXML // fx:id="rescheduleButton"
    private Button rescheduleButton; // Value injected by FXMLLoader

    @FXML // fx:id="visitorNumberPicker"
    private Spinner<Integer> visitorNumberPicker; // Value injected by FXMLLoader
    
    // controller that handles the communication with the server
    private ClientController clientController;
    
    // this is used to make sure a change happened when rescheduling
    LocalDate date = null;
    Integer hour = null;
    
    @FXML
    void enterWaitingListButtonClicked(ActionEvent event) {
    	//@todo
    }

    @FXML
    void rescheduleButtonClicked(ActionEvent event) {
    	buttonBar.setDisable(true);
    	date = datePicker.getValue();
    	hour = hourPicker.getValue();
    	hourPicker.setDisable(false);
    	datePicker.setDisable(false);
    	makeOrderButton.setDisable(false);
    }
    
    @FXML
    void makeOrderButtonClicked(ActionEvent event) {
    	// make sure we get the relevant data
    	if(parkPicker.getValue() == null) {
    		warningMessage("No Park Was Picked");
    		return;
    	}
    	if(datePicker.getValue() == null) {
    		warningMessage("No Date Was Picked");
    		return;
    	}
    	if((date != null && date == datePicker.getValue()) ||
    			(hour != null && hour == hourPicker.getValue())) {
    		warningMessage("No Rescheduling happened");
    		return;
    	}
    	if(email.getText().isEmpty()) {
    		warningMessage("No Email Was Entered");
    		return;
    	}
    	if(!email.getText().contains("@")) {
    		warningMessage("Bad Email");
    		return;
    	}
    	
    	makeOrderButton.setDisable(true);
    	// we lock order details in
    	disableFields();
    	
    	Order o = new Order(datePicker.getValue(), visitorNumberPicker.getValue(),
    			Integer.parseInt(clientController.getId()), parkPicker.getValue());
    	
    	if (guideCheckbox.isSelected()) {
    		o.setOrderType(Order.ORDER_TYPE_ORGANIZED);
    		o.setGuideId(Integer.parseInt(clientController.getId()));
    	} else {
    		o.setOrderType(Order.ORDER_TYPE_PRIVATE);
    		o.setGuideId(null);
    	}
    		
    	
    	clientController.sendMessageToServer(new Message(o, Protocol.MAKE_ORDER));
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert buttonBar != null : "fx:id=\"buttonBar\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert datePicker != null : "fx:id=\"datePicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert enterWaitingListButton != null : "fx:id=\"enterWaitingListButton\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert errorLabel != null : "fx:id=\"errorLabel\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert guideCheckbox != null : "fx:id=\"guideCheckbox\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert hourPicker != null : "fx:id=\"hourPicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert makeOrderButton != null : "fx:id=\"makeOrderButton\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert parkPicker != null : "fx:id=\"parkPicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert rescheduleButton != null : "fx:id=\"rescheduleButton\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        assert visitorNumberPicker != null : "fx:id=\"visitorNumberPicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
        
        //disable button bar since we don't want those buttons available from the start
        buttonBar.setDisable(true);
        // make sure make order button is enabled
        makeOrderButton.setDisable(false);
        //set the hour comboBox to have range 0-23, starting from 12
        hourPicker.getItems().addAll(IntStream.rangeClosed(
        		ConstantsUI.MIN_HOUR, ConstantsUI.MAX_HOUR)
        	             .boxed().collect(Collectors.toList()));
        //set the number of visitors picker to have range 1-15, starting from 1
        visitorNumberPicker.setValueFactory(
        		new SpinnerValueFactory.IntegerSpinnerValueFactory(
        				ConstantsUI.MIN_VISITORS, ConstantsUI.MAX_VISITORS, 
        				ConstantsUI.START_VISITORS));
        
        // setting up the date picker
        // grays out all dates before the current date, makes them unselectable
        datePicker.setEditable(false);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) return;
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        });
        
        /*request park names*/
        requestActiveParkList();
        
        

        /*@todo
         * deal with X
         * */
    }
    
    private void warningMessage(String s) {
    	errorLabel.setText(s);
    }
    
    private void disableFields() {
    	parkPicker.setDisable(true);
    	datePicker.setDisable(true);
    	hourPicker.setDisable(true);
    	visitorNumberPicker.setDisable(true);
    	email.setDisable(true);
    	guideCheckbox.setDisable(true);
    }
    
    private void enableFields() {
    	parkPicker.setDisable(false);
    	datePicker.setDisable(false);
    	hourPicker.setDisable(false);
    	visitorNumberPicker.setDisable(false);
    	email.setDisable(false);
    	guideCheckbox.setDisable(false);
    }
    
    public void loadParkNames(List<String> parkNames) {
    	if(parkNames == null)
    		return;
    	parkPicker.getItems().addAll(parkNames);
    }
    
    private void requestActiveParkList() {
    	clientController.sendMessageToServer(new Message(null, Protocol.GET_PARK_NAMES));
    }
    

    
    public void setClientController(ClientController c) {
    	if(c == null)
    		return;
    	clientController = c;
    	c.addMakeOrderObserver(this);
    }

	@Override
	public void onParkNamesReceived(List<String> parkNames) {
		loadParkNames(parkNames);	
	}
	
	@Override
	public void onMakeOrderServerResponse(Message m) {
		if (m == null) {
			warningMessage("An unknown error occurred.");
			enableFields();
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_SUCCESS) {
			warningMessage("Order made!");
			//@todo update order table to include new order
			// to do this we need to add to the table observer a new method that adds an order
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_TIME) {
			warningMessage("Visit time is unavailable, please Enter Waiting List or Reschedule");
			buttonBar.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_NOT_GUIDE) {
			warningMessage("You are not a guide, cannot book organized visit");
			guideCheckbox.setSelected(false);
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL) {
			warningMessage("Unknown Error occurred");
			enableFields();
			makeOrderButton.setDisable(false);
		}
		
		
	}
}

