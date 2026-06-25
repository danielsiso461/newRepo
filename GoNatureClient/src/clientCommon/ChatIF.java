package clientCommon;

import common.Message;

/**
 * Interface for classes that receive messages from the server.
 */
public interface ChatIF {
    /**
     * Displays a message received from the server.
     *
     * @param message the message received from the server
     */
    void display(Message message);
}