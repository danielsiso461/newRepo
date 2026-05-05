package client;

/**
 * Sample Skeleton for 'OrderUpdatePage.fxml' Controller Class
 */

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

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

    @FXML
    void OrderUpdatePageCancelButtonHandler(ActionEvent event) {
    	
    }

    @FXML
    void OrderUpdatePageUpdateButtonHandler(ActionEvent event) {
    	//need to check if the date wasnt changed then spinner needs to change
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert OrderUpdatePageCancelButton != null : "fx:id=\"OrderUpdatePageCancelButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDatePicker != null : "fx:id=\"OrderUpdatePageDatePicker\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageSpinner != null : "fx:id=\"OrderUpdatePageSpinner\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageUpdateButton != null : "fx:id=\"OrderUpdatePageUpdateButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        OrderUpdatePageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15, 0));
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
    }

}
