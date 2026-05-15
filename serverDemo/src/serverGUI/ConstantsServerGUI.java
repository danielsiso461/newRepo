package serverGUI;

/*
 * this class holds constants relevant for the server GUI
 * including FXML paths and default configuration values
 */
public class ConstantsServerGUI {

	/*
	 * path of the client connection table FXML file
	 */
	protected static final String USER_TABLE =
			"/serverGUI/ClientConnectionTable.fxml";

	/*
	 * the default server port
	 */
	public static final int DEFAULT_PORT = 5555;

	/*
	 * private constructor to prevent object creation
	 */
	private ConstantsServerGUI() {}
}