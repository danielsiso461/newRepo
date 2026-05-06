package trash;

/**
 * Sample Skeleton for 'OrderDisplayPage.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class OrderDisplayPage {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="OrderDisplayPageButton"
    private Button OrderDisplayPageButton; // Value injected by FXMLLoader

    @FXML // fx:id="OrderDisplayPageLabelConfirmationCode"
    private Label OrderDisplayPageLabelConfirmationCode; // Value injected by FXMLLoader

    @FXML // fx:id="OrderDisplayPageLabelDateOfPlacingOrder"
    private Label OrderDisplayPageLabelDateOfPlacingOrder; // Value injected by FXMLLoader

    @FXML // fx:id="OrderDisplayPageLabelNumberOfVisitors"
    private Label OrderDisplayPageLabelNumberOfVisitors; // Value injected by FXMLLoader

    @FXML // fx:id="OrderDisplayPageLabelOrderDate"
    private Label OrderDisplayPageLabelOrderDate; // Value injected by FXMLLoader

    @FXML // fx:id="OrderDisplayPageLabelOrderNumber"
    private Label OrderDisplayPageLabelOrderNumber; // Value injected by FXMLLoader

    @FXML // fx:id="OrderDisplayPageLabelSubscriberID"
    private Label OrderDisplayPageLabelSubscriberID; // Value injected by FXMLLoader

    @FXML
    void handleButtonClick(ActionEvent event) {

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert OrderDisplayPageButton != null : "fx:id=\"OrderDisplayPageButton\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";
        assert OrderDisplayPageLabelConfirmationCode != null : "fx:id=\"OrderDisplayPageLabelConfirmationCode\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";
        assert OrderDisplayPageLabelDateOfPlacingOrder != null : "fx:id=\"OrderDisplayPageLabelDateOfPlacingOrder\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";
        assert OrderDisplayPageLabelNumberOfVisitors != null : "fx:id=\"OrderDisplayPageLabelNumberOfVisitors\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";
        assert OrderDisplayPageLabelOrderDate != null : "fx:id=\"OrderDisplayPageLabelOrderDate\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";
        assert OrderDisplayPageLabelOrderNumber != null : "fx:id=\"OrderDisplayPageLabelOrderNumber\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";
        assert OrderDisplayPageLabelSubscriberID != null : "fx:id=\"OrderDisplayPageLabelSubscriberID\" was not injected: check your FXML file 'OrderDisplayPage.fxml'.";

    }

}
