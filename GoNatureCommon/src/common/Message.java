package common;

import java.io.Serializable;

/**
 * Represents a message passed between the client and the server.
 * <p>
 * Each message contains:
 * </p>
 * <ul>
 * <li>A protocol type that identifies the message purpose</li>
 * <li>Data associated with the message</li>
 * </ul>
 */
public class Message implements Serializable {

    /**
     * Serial version UID used for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The protocol type of the message.
     */
    private Protocol type;

    /**
     * The data carried by the message.
     */
    private Object data;

    /**
     * Creates a new message with the given data and protocol type.
     *
     * @param m the data to send in the message
     * @param type the protocol type of the message
     */
    public Message(Object m, Protocol type) {
        this.type = type;
        data = m;
    }

    /**
     * Returns the protocol type of the message.
     *
     * @return the protocol type
     */
    public Protocol getType() {
        return type;
    }

    /**
     * Returns the data stored in the message.
     *
     * @return the message data
     */
    public Object getData() {
        return data;
    }
}