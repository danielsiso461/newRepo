package clientGUI;
/*
 * this class holds constants relevant to the client
 */
public final class ConstantsUI {
	/*
	 * holds the UI pages paths
	 * */
	protected static final String 
			orderTable = "/clientGUI/OrderTableDisplayPage.fxml",
			updatePage = "/clientGUI/OrderUpdatePage.fxml",
			welcomePage = "/clientGUI/WelcomePage.fxml",
			makeOrderPage = "/clientGUI/MakeOrderPage.fxml";
	
	/*
	 * holds starting values for certain UI elements
	 * */
	protected static final int
			MIN_VISITORS = 1,
			START_VISITORS = 1;
	
	private ConstantsUI() {}
}
