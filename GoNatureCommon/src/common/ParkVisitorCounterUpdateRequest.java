package common;

import java.io.Serializable;

/**
 * Request for updating the current visitor counter of a park.
 */
public class ParkVisitorCounterUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACTION_ENTRY = "entry";
    public static final String ACTION_EXIT = "exit";

    private int parkId;
    private int employeeId;
    private String actionType;
    private int amount;

    public ParkVisitorCounterUpdateRequest(int parkId, int employeeId,
            String actionType, int amount) {

        this.parkId = parkId;
        this.employeeId = employeeId;
        this.actionType = actionType;
        this.amount = amount;
    }

    public int getParkId() {
        return parkId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getActionType() {
        return actionType;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isEntryAction() {
        return ACTION_ENTRY.equals(actionType);
    }

    public boolean isExitAction() {
        return ACTION_EXIT.equals(actionType);
    }

    @Override
    public String toString() {
        return "ParkVisitorCounterUpdateRequest{"
                + "parkId=" + parkId
                + ", employeeId=" + employeeId
                + ", actionType='" + actionType + '\''
                + ", amount=" + amount
                + '}';
    }
}