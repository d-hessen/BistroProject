package dataLayer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import common.Status;

/**
 * Represents a dining visit in the restaurant.
 * A visit may be created from a reservation or as a walk in.
 */
public class Visit implements Serializable {
	
	/**
     * Table assigned to the visit.
     */
    private Table table;

    /**
     * Reservation associated with the visit, if exists.
     */
    private Reservation reservation;

    /**
     * Guest details for the visit.
     */
    private Guest guest;

    /**
     * Indicates whether the visit is currently active.
     */
    private boolean isActive;

    /**
     * Unique identifier of the visit.
     */
    private Integer visitId;

    /**
     * Visit start time.
     */
    private DateTime startTime;

    /**
     * Visit end time.
     */
    private DateTime endTime;

    /**
     * Bill associated with the visit.
     */
    private Bill billOfVisit;

    /**
     * Number of guests in the visit.
     */
    private Integer partySize;

    /**
     * Verification code used for identification.
     */
    private String verificationCode;

    /**
     * Waiting list identifier, if the visit is waiting.
     */
    private Integer waitingId;

    /**
     * Current status of the visit.
     */
    private Status status;
	
    /**
     * Creates a visit based on an existing reservation.
     *
     * @param reservation reservation data
     * @param table assigned table
     */
	public Visit(Reservation reservation, Table table) {
		if(reservation != null) {
			this.reservation = reservation;
			if(reservation.getGuest() instanceof Member) {
				this.guest = (Member)reservation.getGuest();
			}else {
				this.guest = reservation.getGuest();
			}
			setActive(false);
			this.table = table;
			setPartySize(reservation.getNumberOfGuests());
			}
		else {
			System.err.println("Use another constructor Visit(Guest)");
		}
	}
	
	/**
     * Creates a visit based on a reservation with explicit active state.
     *
     * @param reservation reservation data
     * @param table assigned table
     * @param isActive active state
     */
	public Visit(Reservation reservation, Table table, boolean isActive) {
		if(reservation != null) {
			this.guest = reservation.getGuest();
			setTable(table);	
			this.reservation = reservation;
			this.isActive = isActive;
			this.partySize = reservation.getMemberId();
			this.verificationCode = reservation.getVerificationCode();
		}
		else {
			System.err.println("Use another constructor Visit(Guest)");
		}
	}
	
	/**
     * Creates a walk in visit for a guest.
     *
     * @param guest guest details
     * @param table assigned table
     */
	public Visit(Guest guest, Table table) {
		if(guest instanceof Member) {
			this.guest = (Member) guest;
		} else {
			this.guest = guest;
		}
		setTable(table);
		setActive(false);
	}
	
    /** @return assigned table */
	public Table getTable() {
		return table;
	}

    /** @return reservation associated with the visit */
	public Reservation getReservation() {
		return reservation;
	}

    /** @return guest details */
	public Guest getGuest() {
		return guest;
	}

    /** @return true if visit is active */
	public boolean isActive() {
		return isActive;
	}

    /** @return visit identifier */
	public Integer getVisitId() {
		return visitId;
	}

    /** @return visit start time */
	public DateTime getStartTime() {
		return startTime;
	}

    /** @return visit end time */
	public DateTime getEndTime() {
		return endTime;
	}

    /** @param table assigned table */
	public void setTable(Table table) {
		this.table = table;
	}

    /** @param visitId visit identifier */
	public void setVisitId(Integer visitId) {
		this.visitId = visitId;
	}

    /** @param isActive active state */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

    /** @param reservation reservation data */
	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}

    /** @param guest guest details */
	public void setGuest(Guest guest) {
		this.guest = guest;
	}

    /** @param startTime visit start time */
	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

    /** @param endTime visit end time */
	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}

    /** @return visit bill */
	public Bill getBillOfVisit() {
		return billOfVisit;
	}

    /** @param billOfVisit visit bill */
	public void setBillOfVisit(Bill billOfVisit) {
		this.billOfVisit = billOfVisit;
	}

    /** @return number of guests */
	public Integer getPartySize() {
		return partySize;
	}

    /** @param partySize number of guests */
	public void setPartySize(Integer partySize) {
		this.partySize = partySize;
	}

    /** @return verification code */
	public String getVerificationCode() {
		return verificationCode;
	}

    /** @param verificationCode verification code */
	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
	
    /** @return waiting list ID */
	public Integer getWaitingId() {
		return waitingId;
	}

    /** @param waitingId waiting list ID */
	public void setWaitingId(Integer waitingId) {
		this.waitingId = waitingId;
	}

    /** @return visit status */
	public Status getStatus() {
		return status;
	}

    /** @param status visit status */
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
