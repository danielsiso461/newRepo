package clientGUI;

/*
 * this class holds constants relevant to the client
 */
public final class ConstantsUI {
	/*
	 * holds the UI pages paths
	 */
	protected static final String orderTable = "/clientGUI/OrderTableDisplayPage.fxml",
			updatePage = "/clientGUI/OrderUpdatePage.fxml",
			welcomePage = "/clientGUI/WelcomePage.fxml",
			makeOrderPage = "/clientGUI/MakeOrderPage.fxml";

	/*
	 * Constants for visitor number and hour pickers.
	 */
	protected static final int
			MIN_VISITORS = 1,
			MAX_VISITORS = 15,
			START_VISITORS = 1,
			MIN_HOUR = 0,
			MAX_HOUR = 23;

	private ConstantsUI() {}
}