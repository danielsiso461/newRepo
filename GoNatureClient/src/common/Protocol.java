package common;
/*
 * this enum is used to identify the message type passed between server and client
 */
public enum Protocol {
	CLIENT_CONNECT,
	MAKE_ORDER,
	UPDATE_ORDER,
	UPDATE_ORDER_SUCCESS,
	UPDATE_ORDER_FAILURE,
	CLIENT_DISCONNECT_SERVER,
	CLIENT_DISCONNECT_USER,
	RETURN_ORDER
}
