package common;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * this class represents a visit to be logged in the DB
 */
public class Visit implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the id of the visit
     */
    private int visitId;
    /**
     * the corresponding order's number
     */
    private Integer orderNumber;
    /**
     * the park's id
     */
    private int parkId;
    /**
     * the subscriber's id
     */
    private Integer subscriberId;
    /**
     * the visit type
     */
    private String visitType;
    /**
     * the number of visitors in the visit
     */
    private int actualNumberOfVisitors;
    /**
     * the entry time
     */
    private LocalDateTime entryTime;
    /**
     * the exit time
     */
    private LocalDateTime exitTime;
    /**
     * which employee handled the entry
     */
    private Integer handledByEmployeeId;
    /**
     * which employee handled the exit
     */
    private Integer exitHandledByEmployeeId;
    /**
     * which method was used as identification
     */
    private String identificationMethod;
    /**
     * constructor for a visit object
     * @param visitId the visit's id
     * @param orderNumber the order's number
     * @param parkId the park's id
     * @param subscriberId the subscriber's id
     * @param visitType the visit type
     * @param actualNumberOfVisitors the number of visitors
     * @param entryTime the time of entry
     * @param exitTime the time of exit
     * @param handledByEmployeeId the id of the employee that handled the entry
     * @param exitHandledByEmployeeId the id of the employee that handled the exit
     * @param identificationMethod which method was used for identification
     */
    public Visit(int visitId, Integer orderNumber, int parkId, Integer subscriberId,
            String visitType, int actualNumberOfVisitors,
            LocalDateTime entryTime, LocalDateTime exitTime,
            Integer handledByEmployeeId, Integer exitHandledByEmployeeId,
            String identificationMethod) {

        this.visitId = visitId;
        this.orderNumber = orderNumber;
        this.parkId = parkId;
        this.subscriberId = subscriberId;
        this.visitType = visitType;
        this.actualNumberOfVisitors = actualNumberOfVisitors;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.handledByEmployeeId = handledByEmployeeId;
        this.exitHandledByEmployeeId = exitHandledByEmployeeId;
        this.identificationMethod = identificationMethod;
    }
    /**
     * getter for the visit's id
     * @return the visit's id
     */
    public int getVisitId() {
        return visitId;
    }
    /**
     * getter for the order's number
     * @return the order's number
     */
    public Integer getOrderNumber() {
        return orderNumber;
    }
    /**
     * getter for the park's id
     * @return the park's id
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * getter for the subscriber's id
     * @return the subscriber's id
     */
    public Integer getSubscriberId() {
        return subscriberId;
    }
    /**
     * getter for the visit type
     * @return the type of visit
     */
    public String getVisitType() {
        return visitType;
    }
    /**
     * getter for the number of visitors
     * @return the number of visitors
     */
    public int getActualNumberOfVisitors() {
        return actualNumberOfVisitors;
    }
    /**
     * getter for the entry time of the visitors
     * @return the entry time of the visitors
     */
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    /**
     * getter for the exit time of the visitors
     * @return the exit time of the visitors
     */
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    /**
     * getter for the id of the employee that handled the entry
     * @return the id of the employee that handled the entry
     */
    public Integer getHandledByEmployeeId() {
        return handledByEmployeeId;
    }
    /**
     * getter for the id of the employee that handled the exit
     * @return the id of the employee that handled the exit
     */
    public Integer getExitHandledByEmployeeId() {
        return exitHandledByEmployeeId;
    }
    /**
     * getter for the identification method
     * @return the identification method
     */
    public String getIdentificationMethod() {
        return identificationMethod;
    }
    /**
     * standard toString method
     */
    @Override
    public String toString() {
        return "Visit{" +
                "visitId=" + visitId +
                ", orderNumber=" + orderNumber +
                ", parkId=" + parkId +
                ", subscriberId=" + subscriberId +
                ", visitType='" + visitType + '\'' +
                ", actualNumberOfVisitors=" + actualNumberOfVisitors +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", handledByEmployeeId=" + handledByEmployeeId +
                ", exitHandledByEmployeeId=" + exitHandledByEmployeeId +
                ", identificationMethod='" + identificationMethod + '\'' +
                '}';
    }
}