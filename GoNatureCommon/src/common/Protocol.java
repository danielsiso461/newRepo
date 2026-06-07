package common;

/**
 * This enum is used to identify the message type passed between server and client.
 */
public enum Protocol {
	CLIENT_CONNECT,
	MAKE_ORDER,

	UPDATE_ORDER,
	UPDATE_ORDER_SUCCESS,
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

	CLIENT_DISCONNECT_SERVER,
	CLIENT_DISCONNECT_USER,
	RETURN_ORDER,
	GET_ACTIVE_PARKS,
	ACTIVE_PARKS_RESULT,
	PARKS_UPDATED,

	APPROVE_PARK_PARAMETER_CHANGE_REQUEST,
	REJECT_PARK_PARAMETER_CHANGE_REQUEST,

	PARK_PARAMETER_CHANGE_REQUEST_APPROVED,
	PARK_PARAMETER_CHANGE_REQUEST_REJECTED,
	PARK_PARAMETER_CHANGE_REQUEST_FAILURE,

	OCCASIONAL_CUSTOMER_ACCESS_REQUEST,
	OCCASIONAL_CUSTOMER_ACCESS_RESPONSE,
	/**
	 * Sent by the client when the visitor wants to join the waiting list.
	 */
	JOIN_WAITING_LIST_REQUEST,

	/**
	 * Sent by the server when the visitor was added to the waiting list successfully.
	 */
	JOIN_WAITING_LIST_SUCCESS,

	/**
	 * Sent by the server when adding the visitor to the waiting list failed.
	 */
	JOIN_WAITING_LIST_FAILURE,

	SEARCH_SUBSCRIBER_REQUEST,
	SEARCH_SUBSCRIBER_RESPONSE,
	REGISTER_GUIDE_REQUEST,
	REGISTER_GUIDE_RESPONSE
}