package clientGUI;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import clientCommon.MakeOrderObserver;
import clientCommon.WaitingListObserver;
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
import javafx.application.Platform;

import common.*;
/**
 * Sample Skeleton for 'makeOrderPage.fxml' Controller Class
 */

import javafx.scene.control.TextField;

public class MakeOrderPageController implements MakeOrderObserver, WaitingListObserver {

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
    	// make sure the client controller is ready before sending the request
    	if (clientController == null) {
    		warningMessage("Client connection is not ready.");
    		return;
    	}

    	// make sure the user selected a park
    	if (parkPicker.getValue() == null) {
    		warningMessage("No Park Was Picked");
    		return;
    	}

    	// make sure the user selected a date
    	if (datePicker.getValue() == null) {
    		warningMessage("No Date Was Picked");
    		return;
    	}

    	// make sure the user selected an hour
    	if (hourPicker.getValue() == null) {
    		warningMessage("No Hour Was Picked");
    		return;
    	}

    	// create the requested visit date and time from the selected date and hour
    	LocalDateTime requestedOrderDate = datePicker.getValue().atTime(hourPicker.getValue(), 0);

    	// create a waiting list request using the park name from the ComboBox
    	WaitingListMessage waitingListMessage = new WaitingListMessage(
    			Integer.parseInt(clientController.getId()),
    			parkPicker.getValue(),
    			requestedOrderDate,
    			visitorNumberPicker.getValue()
    	);

    	// disable the action buttons while waiting for the server response
    	buttonBar.setDisable(true);

    	warningMessage("Sending waiting list request...");

    	clientController.requestJoinWaitingList(waitingListMessage);
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
    /*
     * This method handles selecting a park from the park ComboBox.
     * 
     * The method currently clears the warning message when the user selects a park.
     * The selected park value is later used when creating an order or joining the
     * waiting list.
     * 
     * @param event the park ComboBox selection event
     */
    @FXML
    void parkPicked(ActionEvent event) {
    	warningMessage("");
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
        //requestActiveParkList();
        /* park names are requested after the client controller is set */
        

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

    	// register this screen to receive make-order responses
    	c.addMakeOrderObserver(this);

    	// register this screen to receive waiting-list responses
    	c.addWaitingListObserver(this);

    	// request park names only after the client controller is ready
    	requestActiveParkList();
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
	/*
	 * This method is called when the server returns a response for joining
	 * the waiting list.
	 * 
	 * @param success true if the request was saved successfully, false otherwise
	 * @param waitingListMessage the waiting list message returned by the server
	 */
	@Override
	public void onJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		if (success && waitingListMessage != null) {
			warningMessage("Added to waiting list. Queue position: "
					+ waitingListMessage.getQueuePosition());
		} else {
			warningMessage("Could not enter waiting list.");
			buttonBar.setDisable(false);
		}
	}
	/*
	 * Handles the result of rejecting a waiting list offer.
	 *
	 * This screen currently does not display waiting list offers, so the method only
	 * clears the warning message when the request succeeds or shows a failure message
	 * when the request fails.
	 *
	 * @param success            true if the waiting list offer was rejected successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	@Override
	public void onRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		if (success) {
			warningMessage("");
		} else {
			warningMessage("Failed to reject waiting list offer.");
		}
	}
	/*
	 * Handles the result of accepting a waiting list offer.
	 *
	 * This screen currently does not display waiting list offers, so the method only
	 * clears the warning message when the request succeeds or shows a failure message
	 * when the request fails.
	 *
	 * @param success            true if the waiting list offer was accepted successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	@Override
	public void onAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		if (success) {
			warningMessage("");
		} else {
			warningMessage("Failed to accept waiting list offer.");
		}
	}

	/*
	 * This method is called when the server disconnects the client.
	 */
	@Override
	public void handleExit() {
		Platform.exit();
		System.exit(0);
	}
	
}

