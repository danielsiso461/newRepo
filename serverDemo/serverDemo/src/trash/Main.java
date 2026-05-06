package trash;
	
import java.util.List;

import client.ConstantsUI;
import client.OrderTableDisplayPage;
import common.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	/*@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}*/
	
	@Override
	public void start(Stage stage) throws Exception {
		//get list from DB @todo
		List<OrderRow> tableData;
		
		// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.OrderTable));
	    Parent root = loader.load();
	    
	    // get controller
	    OrderTableDisplayPage controller = loader.getController();
	    
	    controller.setData(tableData);
	    
	    // show UI
	    Scene scene = new Scene(root);
	    stage.setScene(scene);
	    stage.setTitle("Order Table");
	    stage.show();
	}
	
	/*@Override
    public void start(Stage stage) {        
        VBox g;

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("ex3.fxml"));
			g = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        
        stage.setTitle("Voting Machine");
        stage.setScene(new Scene(g));
        stage.show();
    }*/
	
	public static void main(String[] args) {
		launch(args);
	}
}
