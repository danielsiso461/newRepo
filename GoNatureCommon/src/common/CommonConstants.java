package common;

public class CommonConstants {

	
	private CommonConstants() {
		
	}
	
	/*
	 * the default server port
	 */
	public static final int DEFAULT_PORT = 5555,
							MAX_VISITOR_COUNT = 15,
							MIN_VISITOR_COUNT = 1;
	
	/**
	 * Message type for notifying clients that park data was updated.
	 */
	public static final String PARKS_UPDATED = "PARKS_UPDATED";
}
