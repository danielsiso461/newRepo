package client;

/**
 * Sample Skeleton for 'OrderUpdatePage.fxml' Controller Class
 */

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import common.UpdateMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

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

    private int orderNumber;
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

    public void setOrderData(int orderNumber, LocalDate orderDate, int numberOfVisitors) {
        this.orderNumber = orderNumber;
        this.originalDate = orderDate;
        this.originalVisitors = numberOfVisitors;

        OrderUpdatePageDatePicker.setValue(orderDate);
        OrderUpdatePageSpinner.getValueFactory().setValue(numberOfVisitors);
    }

    @FXML
    void OrderUpdatePageCancelButtonHandler(ActionEvent event) {
        if (prevController != null) {
            prevController.removeOrderFromUpdateWaitingList(orderNumber);
        }

        Stage currentStage = (Stage) OrderUpdatePageCancelButton.getScene().getWindow();
        currentStage.close();

        if (prevStage != null) {
            prevStage.show();
        }
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

        UpdateMessage um =
                new UpdateMessage(dateToSend, visitorsToSend, orderNumber);

        service.requestUpdate(um);
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert OrderUpdatePageCancelButton != null : "fx:id=\"OrderUpdatePageCancelButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDatePicker != null : "fx:id=\"OrderUpdatePageDatePicker\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageSpinner != null : "fx:id=\"OrderUpdatePageSpinner\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageUpdateButton != null : "fx:id=\"OrderUpdatePageUpdateButton\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageDateCheckBox != null : "fx:id=\"OrderUpdatePageDateCheckBox\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";
        assert OrderUpdatePageVisitorsCheckBox != null : "fx:id=\"OrderUpdatePageVisitorsCheckBox\" was not injected: check your FXML file 'OrderUpdatePage.fxml'.";

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