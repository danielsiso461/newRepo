package common;

import java.io.Serializable;

/**
 * Request for updating the current visitor counter of a park.
 */
public class ParkVisitorCounterUpdateRequest implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * a string that represents an entry action
     */
    public static final String ACTION_ENTRY = "entry";
    /**
     * a string that represents an exit action
     */
    public static final String ACTION_EXIT = "exit";
    /**
     * the id of the park
     */
    private int parkId;
    /**
     * the id of the employee
     */
    private int employeeId;
    /**
     * the action type of the request
     */
    private String actionType;
    /**
     * the amount to change the counter by
     */
    private int amount;
    /**
     * constructor for a park's visitor counter update request
     * @param parkId the park's id
     * @param employeeId the id of the employee who made the update
     * @param actionType the type of the action
     * @param amount the amount of visitors to change the counter
     */
    public ParkVisitorCounterUpdateRequest(int parkId, int employeeId,
            String actionType, int amount) {

        this.parkId = parkId;
        this.employeeId = employeeId;
        this.actionType = actionType;
        this.amount = amount;
    }
    /**
     * getter for the park id
     * @return the park id
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * getter for the employee id
     * @return the employee id
     */
    public int getEmployeeId() {
        return employeeId;
    }
    /**
     * getter for the action type
     * @return the action type
     */
    public String getActionType() {
        return actionType;
    }
    /**
     * getter for the amount in the request
     * @return the amount in the request
     */
    public int getAmount() {
        return amount;
    }
    /**
     * checks if the action is an entry action
     * @return true on an entry action
     */
    public boolean isEntryAction() {
        return ACTION_ENTRY.equals(actionType);
    }
    /**
     * checks if the action is an exit action
     * @return true on an exit action
     */
    public boolean isExitAction() {
        return ACTION_EXIT.equals(actionType);
    }
    /**
     * standard toString method
     */
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