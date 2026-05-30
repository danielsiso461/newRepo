package clientGUI;

/**
 * Sample Skeleton for 'makeOrderPage.fxml' Controller Class
 */

import javafx.scene.control.TextField;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
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

public class MakeOrderPageController {

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
    private Spinner<Integer> hourPicker; // Value injected by FXMLLoader

    @FXML // fx:id="makeOrderButton"
    private Button makeOrderButton; // Value injected by FXMLLoader

    @FXML // fx:id="parkPicker"
    private ComboBox<Park> parkPicker; // Value injected by FXMLLoader

    @FXML // fx:id="rescheduleButton"
    private Button rescheduleButton; // Value injected by FXMLLoader

    @FXML // fx:id="visitorNumberPicker"
    private Spinner<Integer> visitorNumberPicker; // Value injected by FXMLLoader

    @FXML
    void enterWaitingListButtonClicked(ActionEvent event) {
    	//@todo
    }

    @FXML
    void rescheduleButtonClicked(ActionEvent event) {
    	//@todo
    }
    
    @FXML
    void makeOrderButtonClicked(ActionEvent event) {
    	if(parkPicker.getValue() == null) {
    		warningMessage("No Park Was Picked");
    		return;
    	}
    	if(datePicker.getValue() == null) {
    		warningMessage("No Date Was Picked");
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
    	
    	//@todo - depending on checkbox make the order
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
        //set the hour spinner to have range 0-23, starting from 12
        hourPicker.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        //set the number of visitors picker to have range 1-15, starting from 1
        visitorNumberPicker.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 1));
        
        // setting up the date picker
        // grays out all dates before the current date, makes the unselectable
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
        
        /*@todo
         * load parks into combo box
         * */
    }
    
    private void warningMessage(String s) {
    	errorLabel.setText(s);
    }
    
}

