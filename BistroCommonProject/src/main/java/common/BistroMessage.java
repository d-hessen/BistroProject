package common;

import java.io.Serializable;

/**
 * Represents a message exchanged between client and server.
 * Encapsulates an action type and an optional data payload.
 */
public class BistroMessage implements Serializable {
    
	/** Serialization version UID. */
    private static final long serialVersionUID = 1L;

    /** Action indicating the intent of the message. */
    private Action action;

    /** Payload associated with the action. */
    private Object data;

    /**
     * Constructs a new BistroMessage with the given action and data.
     *
     * @param action the action type
     * @param data   the payload associated with the action
     */
    public BistroMessage(Action action, Object data) {
        this.action = action;
        this.data = data;
    }
    
    /**
     * Returns the action of this message.
     *
     * @return the action type
     */
    public Action getAction() { return action; }
    
    /**
     * Returns the data payload of this message.
     *
     * @return the message payload
     */
    public Object getData() { return data; }
}
