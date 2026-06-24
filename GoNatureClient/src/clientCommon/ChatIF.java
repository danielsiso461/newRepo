package clientCommon;

import common.Message;

/**
 * Interface for classes that receive messages from the server.
 */
public interface ChatIF {
    void display(Message message);
}