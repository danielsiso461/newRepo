package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


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
	    Parent root = FXMLLoader.load(getClass().
	    		getResource("OrderUpdatePage.fxml"));
	    
	    Scene scene = new Scene(root);
	    stage.setScene(scene);
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
