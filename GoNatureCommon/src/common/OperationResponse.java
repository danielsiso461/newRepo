package common;

import java.io.Serializable;

public class OperationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;

    public OperationResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "OperationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}