package common;
/**
 * this class holds common constants between the server and client
 */
public class CommonConstants {
	/**
	 * private constructor to prevent instantiating the class
	 */
	private CommonConstants() {

	}

	/**
	 * The default server port
	 */
	public static final int DEFAULT_PORT = 5555;
	/**
	 *  the max visitor count allowed in an order	
	 */
	public static final int MAX_VISITOR_COUNT = 15;
	/**
	 * the minimum visitor count allowed in an order
	 */
	public static final int MIN_VISITOR_COUNT = 1;
	/**
	 * the minimum hour in a clock
	 */
	public static final int MIN_HOUR = 0;
	/**
	 * the maximum hour in a clock
	 */
	public static final int MAX_HOUR = 23;
	

	/**
	 * Message type for notifying clients that park data was updated.
	 */
	public static final String PARKS_UPDATED = "PARKS_UPDATED";
}