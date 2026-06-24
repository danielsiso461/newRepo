package clientGUI;

/**
 * This class holds constants relevant to the client.
 */
public final class ConstantsUI {

	/**
	 * The order table's FXML path.
	 */
	protected static final String orderTable = "/clientGUI/OrderTableDisplayPage.fxml";

	/**
	 * The update page's FXML path.
	 */
	protected static final String updatePage = "/clientGUI/OrderUpdatePage.fxml";

	/**
	 * The welcome page's FXML path.
	 */
	protected static final String welcomePage = "/clientGUI/WelcomePage.fxml";

	/**
	 * The make order page's FXML path.
	 */
	protected static final String makeOrderPage = "/clientGUI/MakeOrderPage.fxml";

	/**
	 * The opening screen's FXML path.
	 */
	protected static final String openingPage = "/clientGUI/OpeningScreen.fxml";

	/**
	 * The waiting list page's FXML path.
	 */
	protected static final String waitingListPage = "/clientGUI/WaitingListPage.fxml";

	/**
	 * The park entrance control page's FXML path.
	 */
	protected static final String parkEntranceControlPage = "/clientGUI/ParkEntranceControlPage.fxml";

	/**
	 * Constants for visitor number and hour pickers.
	 */
	protected static final int MIN_VISITORS = 1;
	protected static final int MAX_VISITORS = 15;
	protected static final int START_VISITORS = 1;
	protected static final int MIN_HOUR = 0;
	protected static final int MAX_HOUR = 23;

	/**
	 * Empty private constructor so this class cannot be instantiated.
	 */
	private ConstantsUI() {
	}
}