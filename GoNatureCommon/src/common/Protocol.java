package common;

/**
 * This enum is used to identify the message type passed between server and client.
 */
public enum Protocol {
	/**
	 * connect message from the client
	 */
	CLIENT_CONNECT,
	/**
	 * client request to calculate the entry price
	 */
	CALCULATE_ENTRY_PRICE_REQUEST,
	/**
	 * server response to entry price calculation request
	 */
	CALCULATE_ENTRY_PRICE_RESPONSE,
	/**
	 * client request to get a park's visitor count
	 */
	GET_PARK_VISITOR_COUNTERS_REQUEST,
	/**
	 * server response to a park's visitor counter request
	 */
	PARK_VISITOR_COUNTERS_RESULT,
	/**
	 * client request to update visitor counter of a park
	 */
	UPDATE_PARK_VISITOR_COUNTER_REQUEST,
	/**
	 * server response of visitor counter update request result
	 */
	PARK_VISITOR_COUNTER_UPDATE_RESULT,
	/**
	 * server response to visitor counter update request
	 */
	PARK_VISITOR_COUNTERS_UPDATED,
	/**
	 * client request to get all active park names
	 */
	GET_PARK_NAMES,
	/**
	 * server response holding the park names
	 */
	RETURN_PARK_NAMES_SUCCESS,
	/**
	 * server response failure to return park names
	 */
	RETURN_PARK_NAMES_FAILURE,
	/**
	 * client request to make a new order
	 */
	MAKE_ORDER,
	/**
	 * server response successfully making a new order
	 */
	MAKE_ORDER_SUCCESS,
	/**
	 * server response failing to make a new order
	 */
	MAKE_ORDER_FAIL,
	/**
	 * server response failing to make a new order because the user is not a guide 
	 * and they are asking for an organized visit
	 */
	MAKE_ORDER_FAIL_NOT_GUIDE,
	/**
	 * server response failing to make a new order because there is no 
	 * space for the order at the given time
	 */
	MAKE_ORDER_FAIL_TIME,
	/**
	 * server response failing to make a new order because the user is not subscribed
	 * and they are asking for too many visitors
	 */
	MAKE_ORDER_FAIL_NOT_SUBSCRIBED,
	/**
	 * client request to update an order
	 */
	UPDATE_ORDER,
	/**
	 * server response order update success
	 */
	UPDATE_ORDER_SUCCESS,
	/**
	 * server response order update failure
	 */
	UPDATE_ORDER_FAILURE,

	/**
	 * Sent by the client when the user requests to cancel an existing order.
	 */
	CANCEL_ORDER,

	/**
	 * Sent by the server when the order cancellation was completed successfully.
	 */
	CANCEL_ORDER_SUCCESS,

	/**
	 * Sent by the server when the order cancellation failed.
	 */
	CANCEL_ORDER_FAILURE,
	/**
	 * server message disconnecting the user
	 */
	CLIENT_DISCONNECT_SERVER,
	/**
	 * client message disconnecting from server
	 */
	CLIENT_DISCONNECT_USER,
	/**
	 * client request to get all of the user's orders
	 */
	RETURN_ORDER,
	/**
	 * client request getting all active parks
	 */
	GET_ACTIVE_PARKS,
	/**
	 * server response to active parks request
	 */
	ACTIVE_PARKS_RESULT,
	/**
	 * server response to updating park
	 */
	PARKS_UPDATED,
	/**
	 * client request to approve park parameter change request
	 */
	APPROVE_PARK_PARAMETER_CHANGE_REQUEST,
	/**
	 * client request to reject park parameter change request
	 */
	REJECT_PARK_PARAMETER_CHANGE_REQUEST,
	/**
	 * server response to park parameter change approved
	 */
	PARK_PARAMETER_CHANGE_REQUEST_APPROVED,
	/**
	 * server response to park parameter change rejected
	 */
	PARK_PARAMETER_CHANGE_REQUEST_REJECTED,
	/**
	 * server response park parameter failure
	 */
	PARK_PARAMETER_CHANGE_REQUEST_FAILURE,
	/**
	 * client request to make a park parameter change request
	 */
	CREATE_PARK_PARAMETER_CHANGE_REQUEST,
	/**
	 * server response to creating a park parameter change request
	 */
	PARK_PARAMETER_CHANGE_REQUEST_CREATED,
	/**
	 * client request to get all pending park parameter change requests
	 */
	GET_PENDING_PARK_PARAMETER_CHANGE_REQUESTS,
	/**
	 * server response to get all pending park parameter change requests
	 */
	PENDING_PARK_PARAMETER_CHANGE_REQUESTS_RESULT,
	/**
	 * client request to get a report
	 */
	GET_REPORT_REQUEST,
	/**
	 * server response to get a report request
	 */
	GET_REPORT_RESPONSE,
	/**
	 * client request to allow occasional customer access
	 */
	OCCASIONAL_CUSTOMER_ACCESS_REQUEST,
	/**
	 * server response to occasional customer access request
	 */
	OCCASIONAL_CUSTOMER_ACCESS_RESPONSE,
	/**
	 * client request to allow employee login
	 */
	EMPLOYEE_LOGIN_REQUEST,
	/**
	 * server response to employee login request
	 */
	EMPLOYEE_LOGIN_RESPONSE,
	/**
	 * client request to allow existing customer login
	 */
	EXISTING_CUSTOMER_LOGIN_REQUEST,
	/**
	 * server response to existing customer login request
	 */
	EXISTING_CUSTOMER_LOGIN_RESPONSE,
	/**
	 * client request to register a subscriber
	 */
	REGISTER_SUBSCRIBER_REQUEST,
	/**
	 * server response to subscriber registration
	 */
	REGISTER_SUBSCRIBER_RESPONSE,
	/**
	 * client request to join waiting list
	 */
	JOIN_WAITING_LIST_REQUEST,
	/**
	 * server response on waiting list join success
	 */
	JOIN_WAITING_LIST_SUCCESS,
	/**
	 * server response on waiting list join failure
	 */
	JOIN_WAITING_LIST_FAILURE,
	/**
	 * client request to get waiting offers
	 */
	GET_WAITING_OFFERS_REQUEST,
	/**
	 * server response when successfully got offers
	 */
	GET_WAITING_OFFERS_SUCCESS,
	/**
	 * server response when didn't get offers
	 */
	GET_WAITING_OFFERS_FAILURE,
	/**
	 * client request to reject waiting list offer
	 */
	REJECT_WAITING_OFFER_REQUEST,
	/**
	 * server response on waiting list offer rejection success
	 */
	REJECT_WAITING_OFFER_SUCCESS,
	/**
	 * server response on waiting list offer rejection failure
	 */
	REJECT_WAITING_OFFER_FAILURE,
	/**
	 * client request to accept waiting list offer
	 */
	ACCEPT_WAITING_OFFER_REQUEST,
	/**
	 * server response on waiting list offer acceptance success
	 */
	ACCEPT_WAITING_OFFER_SUCCESS,
	/**
	 * server response on waiting list offer acceptance failure
	 */
	ACCEPT_WAITING_OFFER_FAILURE,
	/**
	 * client request to search for a subscriber
	 */
	SEARCH_SUBSCRIBER_REQUEST,
	/**
	 * server response to subscriber search
	 */
	SEARCH_SUBSCRIBER_RESPONSE,
	/**
	 * client request to register a guide
	 */
	REGISTER_GUIDE_REQUEST,
	/**
	 * server response to guide registration request
	 */
	REGISTER_GUIDE_RESPONSE,
	/**
	 * client request to get park orders
	 */
	GET_PARK_ORDERS_REQUEST,
	/**
	 * server response to get park orders request
	 */
	GET_PARK_ORDERS_RESPONSE,
	/**
	 * client request to get all orders
	 */
	GET_ALL_ORDERS_REQUEST,
	/**
	 * server response to "get all orders" request
	 */
	GET_ALL_ORDERS_RESPONSE,
	/**
	 * client request to logout
	 */
	CLIENT_LOGOUT_USER,
	/**
	 * server response when client logs out successfully
	 */
	CLIENT_LOGOUT_USER_SUCCESS,
	/**
	 * client request to check in order
	 */
	CHECK_IN_ORDER_REQUEST,
	/**
	 * server response when successfully checking in an order
	 */
	CHECK_IN_ORDER_SUCCESS,
	/**
	 * server response when failed to check in an order
	 */
	CHECK_IN_ORDER_FAILURE,
	/**
	 * client request to check out a visit
	 */
	CHECK_OUT_VISIT_REQUEST,
	/**
	 * server response when successfully checking out a visit
	 */
	CHECK_OUT_VISIT_SUCCESS,
	/**
	 * server response when failed to check out a visit
	 */
	CHECK_OUT_VISIT_FAILURE,
	/**
	 * client request to check in an occasional visitor
	 */
	OCCASIONAL_VISIT_REQUEST,
	/**
	 * server response when successfully checking in an occasional visitor
	 */
	OCCASIONAL_VISIT_SUCCESS,
	/**
	 * server response when failing to check in an occasional visitor
	 */
	OCCASIONAL_VISIT_FAILURE,
	/**
	 * client request to get current visitors
	 */
	GET_CURRENT_VISITORS_REQUEST,
	/**
	 * server response when successfully getting current visitors
	 */
	GET_CURRENT_VISITORS_SUCCESS,
	/**
	 * server response when failing to get current visitors
	 */
	GET_CURRENT_VISITORS_FAILURE,
	/**
	 * client request to search user information
	 */
	SEARCH_USER_INFORMATION_REQUEST,
	/**
	 * server response to getting user information
	 */
	SEARCH_USER_INFORMATION_RESPONSE,
	//reminder----------------------------------------------------------------------------
	/**
	 * server message to remind about orders
	 */
	ORDER_REMINDER,
	/**
	 * client response accepting the reminder
	 */
	ACCEPT_ORDER_REMINDER,
	/**
	 * client response declining the reminder
	 */
	DECLINE_ORDER_REMINDER,
	/**
	 * client response accepting reminder
	 */
	ACCEPT_ORDER_REMINDER_CONFIRMATION,
	/**
	 * client response declining reminder
	 */
	DECLINE_ORDER_REMINDER_CONFIRMATION,
	/**
	 * server response error regarding reminder answer
	 */
	ERROR_ORDER_REMINDER_CONFIRMATION
}
