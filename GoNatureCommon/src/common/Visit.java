package common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Visit implements Serializable {

    private static final long serialVersionUID = 1L;

    private int visitId;
    private Integer orderNumber;
    private int parkId;
    private Integer subscriberId;
    private String visitType;
    private int actualNumberOfVisitors;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer handledByEmployeeId;
    private Integer exitHandledByEmployeeId;
    private String identificationMethod;

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

    public int getVisitId() {
        return visitId;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public int getParkId() {
        return parkId;
    }

    public Integer getSubscriberId() {
        return subscriberId;
    }

    public String getVisitType() {
        return visitType;
    }

    public int getActualNumberOfVisitors() {
        return actualNumberOfVisitors;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public Integer getHandledByEmployeeId() {
        return handledByEmployeeId;
    }

    public Integer getExitHandledByEmployeeId() {
        return exitHandledByEmployeeId;
    }

    public String getIdentificationMethod() {
        return identificationMethod;
    }

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