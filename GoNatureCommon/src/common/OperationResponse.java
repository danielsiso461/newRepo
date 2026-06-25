package common;

import java.io.Serializable;
/**
 * represents the outcome of an operation
 * this class stores whether the operation succeeded,
 * an optional message, and any data returned
 */
public class OperationResponse implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * indicates whether the operation was successful
     */
    private boolean success;
    /**
     * additional information about the operation result
     */
    private String message;
    /**
     * data returned by the operation
     */
    private Object data;
    /**
     * creates a new operation response
     *
     * @param success true if the operation succeeded
     * @param message a message describing the result
     * @param data data returned by the operation
     */
    public OperationResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    /**
     * returns whether the operation succeeded
     *
     * @return true if the operation was successful
     */
    public boolean isSuccess() {
        return success;
    }
    /**
     * returns the result message
     *
     * @return the message associated with the operation
     */
    public String getMessage() {
        return message;
    }
    /**
     * returns the data produced by the operation
     *
     * @return the returned data
     */
    public Object getData() {
        return data;
    }
    /**
     * standard toString method
     * @return a string containing the response details
     */
    @Override
    public String toString() {
        return "OperationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}