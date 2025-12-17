package common;

import java.io.Serializable;

public class BistroMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Action action; // The intent (e.g., GET_RESERVATION)
    private Object data;   // The payload (e.g., 5, or a Reservation object)

    public BistroMessage(Action action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Action getAction() { return action; }
    public Object getData() { return data; }
}
