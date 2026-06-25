package databaseControllers;

/**
 * Contains constant names for database tables and views.
 * 
 * This utility class centralizes table and view names used by the database
 * connector classes, reducing hard-coded strings throughout the project.
 */
public final class ConstantsDBTableNames {

	/**
	 * Database table that stores park orders.
	 */
	protected static final String ORDER = "order";

	/**
	 * Database table that stores employee details.
	 */
	protected static final String EMPLOYEE = "employee";

	/**
	 * Database table that stores payment bill records.
	 */
	protected static final String BILL = "bill";

	/**
	 * Database table that stores entry pricing model data.
	 */
	protected static final String ENTRY_PRICING_MODEL = "entry_pricing_model";

	/**
	 * Database table that stores guide details.
	 */
	protected static final String GUIDE = "guide";

	/**
	 * Database table that stores park details.
	 */
	protected static final String PARK = "park";

	/**
	 * Database table that stores subscriber details.
	 */
	protected static final String SUBSCRIBER = "subscriber";

	/**
	 * Database table that stores actual visit records.
	 */
	protected static final String VISIT = "visit";

	/**
	 * Database table that stores waiting list records.
	 */
	protected static final String WAITING_LIST = "waiting_list";

	/**
	 * Database table that stores system notifications.
	 */
	protected static final String NOTIFICATION = "notification";

	/**
	 * Database table that stores park parameter change requests.
	 */
	protected static final String PARK_PARAMETER_CHANGE_REQUEST = "park_parameter_change_request";

	/**
	 * Database view used for visit duration reports.
	 */
	protected static final String VISIT_DURATION_REPORT = "visit_duration_report";

	/**
	 * Database view used for cancellation reports.
	 */
	protected static final String CANCELLATION_REPORT = "cancellation_report";

	/**
	 * Database view used for calculating visit prices.
	 */
	protected static final String VISIT_PRICE_CALCULATION = "visit_price_calculation";

	/**
	 * Database view used for visitor reports grouped by visitor type.
	 */
	protected static final String VISITOR_REPORT_BY_TYPE = "visitor_report_by_type";

	/**
	 * Database view used for notification reports.
	 */
	protected static final String NOTIFICATION_REPORT = "notification_report";

	/**
	 * Database view used for park usage reports.
	 */
	protected static final String PARK_USAGE_REPORT = "park_usage_report";

	/**
	 * Database view used for revenue reports grouped by park.
	 */
	protected static final String REVENUE_REPORT_BY_PARK = "revenue_report_by_park";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private ConstantsDBTableNames() {
	}
}