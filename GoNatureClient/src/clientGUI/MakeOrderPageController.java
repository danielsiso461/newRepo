package clientGUI;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import clientCommon.MakeOrderObserver;
import clientController.ClientController;
/**
 * Sample Skeleton for 'makeOrderPage.fxml' Controller Class
 */
import common.CommonConstants;
import common.Message;
import common.Order;
import common.Protocol;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
/*
 * this class is the UI controller for the make order page
 */
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
    
    /* controller that handles the communication with the server */
    private ClientController clientController;
    /* holds the last controller, which is supposed to be a runnable object */
    private Runnable prevController;
    /* holds the scene of the previous page */
    private Scene prevScene;
    
    /* holds the previous date
     * this is used to make sure a change happened when rescheduling */
    private LocalDate date = null;
    /* holds the previous hour
     * this is used to make sure a change happened when rescheduling */
    private Integer hour = null;
    
    /* keeps track if the user left the make order scene */
    private boolean switchedScene = false;
    /* keeps track if the user made an order through this instance of make order controller */
    private boolean orderMade = false;
    
    /*
     * this method triggers when the enter waiting list button is clicked
     * @param event the button click event
     */
    @FXML
    void enterWaitingListButtonClicked(ActionEvent event) {
    	//@todo
    }
    
    /*
     * this method triggers when the enter reschedule button is clicked
     * it handles enabling relevant fields
     * @param event the button click event
     */
    @FXML
    void rescheduleButtonClicked(ActionEvent event) {
    	buttonBar.setDisable(true);
    	date = datePicker.getValue();
    	hour = hourPicker.getValue();
    	hourPicker.setDisable(false);
    	datePicker.setDisable(false);
    	makeOrderButton.setDisable(false);
    }
    
    /*
     * this method triggers when the enter make order button is clicked
     * it handles checking the entered values and sending the order to the server
     * @param event the button click event
     */
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
    	if((date != null && date == datePicker.getValue()) &&
    			(hour != null && hour == hourPicker.getValue())) {
    		warningMessage("No Rescheduling happened");
    		return;
    	}
    	if(hourPicker.getValue() == null) {
    		warningMessage("No Hour Was Picked");
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
    			Integer.parseInt(clientController.getId()), parkPicker.getValue(), 
    			hourPicker.getValue(), email.getText());
    	
    	if (guideCheckbox.isSelected()) {
    		o.setOrderType(Order.ORDER_TYPE_ORGANIZED);
    	} else {
    		o.setOrderType(Order.ORDER_TYPE_PRIVATE);
    		o.setGuideId(null);
    	}
    		
    	orderMade = true;
    	clientController.sendMessageToServer(new Message(o, Protocol.MAKE_ORDER));
    }
    
    /* this method initializes the make order page
     * handles certain components behaviors and the red X button behavior
     * */
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
        		CommonConstants.MIN_HOUR, CommonConstants.MAX_HOUR)
        	             .boxed().collect(Collectors.toList()));
        //set the number of visitors picker to have range 1-15, starting from 1
        visitorNumberPicker.setValueFactory(
        		new SpinnerValueFactory.IntegerSpinnerValueFactory(
        				CommonConstants.MIN_VISITOR_COUNT, CommonConstants.MAX_VISITOR_COUNT, 
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
        

        
     // this handles returning to order table when pressing the red X button
     	Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			Stage stage = (Stage) makeOrderButton.getScene().getWindow();
     			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
     				@Override
     				public void handle(WindowEvent event) {
     					event.consume();
     					switchScene();
     				}
     			});
     		}
     	});
    }
    
    /* this method changes the error label of the form according to the given message
	 * @param s the given message 
     * */
    private void warningMessage(String s) {
    	errorLabel.setText(s);
    }
    
    /* this method disables all of the form's fields*/
    private void disableFields() {
    	parkPicker.setDisable(true);
    	datePicker.setDisable(true);
    	hourPicker.setDisable(true);
    	visitorNumberPicker.setDisable(true);
    	email.setDisable(true);
    	guideCheckbox.setDisable(true);
    }
    
    /* this method enables all of the form's fields*/
    private void enableFields() {
    	parkPicker.setDisable(false);
    	datePicker.setDisable(false);
    	hourPicker.setDisable(false);
    	visitorNumberPicker.setDisable(false);
    	email.setDisable(false);
    	guideCheckbox.setDisable(false);
    }
    
    /* this method loads the given park names into the relevant controller component
     * @param parkNames the list of park Names
     * */
    public void loadParkNames(List<String> parkNames) {
    	if(parkNames == null)
    		return;
    	parkPicker.getItems().addAll(parkNames);
    }
    
    /* this method sends a request to the server to get the list of park names
     * */
    public void requestActiveParkList() {
    	clientController.sendMessageToServer(new Message(null, Protocol.GET_PARK_NAMES));
    }
    

    /* this method handles setting a client controller for this UI controller
     * it also adds this controller as a make order observer on the client controller
     * @param c the client controller
     */ 
    public void setClientController(ClientController c) {
    	if(c == null)
    		return;
    	clientController = c;
    	c.addMakeOrderObserver(this);
    }
    
    /* this method handles keeping track of the previous scene
     * @param prevScene the previous scene
     */ 
    public void setPrevScene(Scene prevScene) {
    	if(prevScene == null)
    		return;
    	this.prevScene = prevScene;
    	switchedScene = false;
    }
    
    /* this method handles keeping track of the previous controller
     * @param prevController the previous controller
     */    
    public void setPrevController(Runnable prevController) {
    	if(prevController == null)
    		return;
    	this.prevController = prevController;
    }
    
    /*
     * this method handles receiving the park names from the server
     * @param parkNames the park name list
     * */
	@Override
	public void onParkNamesReceived(List<String> parkNames) {
		loadParkNames(parkNames);	
	}
	
	/*
	 * this method handles the response from the server
	 * @param m the message from the server
	 */	
	@Override
	public void onMakeOrderServerResponse(Message m) {
		String declineMsg = null;
		if(!orderMade) {
			return;
		}
		if (m == null) {
			declineMsg = "Unknown";
			warningMessage("An unknown error occurred.");
			enableFields();
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_SUCCESS) {
			Order o = (Order) m.getData();
			MakePopUp.makePopup("Order made!", null);
			MakePopUp.makePopup("EMAIL SIMULATION", "Order made! " + o.getEmail());
			if(o.getIsSubscribed()) {
				MakePopUp.makePopup("PHONE SIMULATION", "Order made! " + o.getPhoneNumber());
			}
			if(!switchedScene)
				switchScene();
			clientController.removeMakeOrderObserver(this);
			return;
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_TIME) {
			declineMsg = "Visit time is unavailable";
			warningMessage("Visit time is unavailable, please Enter Waiting List or Reschedule");
			buttonBar.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_NOT_GUIDE) {
			declineMsg = "You are not a guide";
			warningMessage("You are not a guide, cannot book organized visit");
			guideCheckbox.setSelected(false);
			enableFields();
			guideCheckbox.setDisable(true);
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_NOT_SUBSCRIBED) {
			declineMsg = "Too many visitors";
			warningMessage("You are not subscribed, cannot book a visit for multiple people");
			visitorNumberPicker.setDisable(false);
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL) {
			declineMsg = "Unknown server error";
			warningMessage("Unknown Error occurred on the server");
			enableFields();
			makeOrderButton.setDisable(false);
		} else {
			declineMsg = "Unknown parsing error";
			warningMessage("Unknown Error occurred parsing response");
			enableFields();
			makeOrderButton.setDisable(false);
		}
		if (switchedScene) {
			clientController.removeMakeOrderObserver(this);
			if(orderMade)
				MakePopUp.makePopup("Order declined!", declineMsg);
			return;
		}
		orderMade = false;
	}
	
	/*
	 * this method handles switching the current scene back the previous one*/
	private void switchScene() {
		Stage stage = (Stage) makeOrderButton.getScene().getWindow();
		stage.setScene(prevScene);
    	stage.setTitle("Order Table Page");
    	switchedScene = true;
    	prevController.run();
		stage.show();
	}
}

