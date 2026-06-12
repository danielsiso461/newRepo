package common;

public class CommonConstants {

	private CommonConstants() {

	}

	/*
	 * The default server port and general system constants.
	 */
	public static final int DEFAULT_PORT = 5555,
							MAX_VISITOR_COUNT = 15,
							MAX_VISITORS = 15,
							MIN_VISITOR_COUNT = 1,
							MIN_HOUR = 0,
							MAX_HOUR = 23;

	/**
	 * Message type for notifying clients that park data was updated.
	 */
	public static final String PARKS_UPDATED = "PARKS_UPDATED";
}