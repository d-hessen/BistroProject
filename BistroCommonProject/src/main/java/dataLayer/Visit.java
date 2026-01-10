package dataLayer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Visit implements Serializable {
	private Table table;
	private Reservation reservation; //If visit was booked there will be instance
	private Guest guest; //main guest details
	private boolean isActive;
	private Integer visitId;
	private DateTime startTime;
	private DateTime endTime;
	private Bill billOfVisit;
	private Integer partySize;
	private String verificationCode;
	private Integer waitingId;
	
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
	
	public Visit(Guest guest, Table table) {
		if(guest instanceof Member) {
			this.guest = (Member) guest;
		} else {
			this.guest = guest;
		}
		setTable(table);
		setActive(false);
	}
	
	
	public Table getTable() {
		return table;
	}

	public Reservation getReservation() {
		return reservation;
	}

	public Guest getGuest() {
		return guest;
	}

	public boolean isActive() {
		return isActive;
	}

	public Integer getVisitId() {
		return visitId;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public void setVisitId(Integer visitId) {
		this.visitId = visitId;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}

	public void setGuest(Guest guest) {
		this.guest = guest;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}

	public Bill getBillOfVisit() {
		return billOfVisit;
	}

	public void setBillOfVisit(Bill billOfVisit) {
		this.billOfVisit = billOfVisit;
	}

	public Integer getPartySize() {
		return partySize;
	}

	public void setPartySize(Integer partySize) {
		this.partySize = partySize;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public Integer getWaitingId() {
		return waitingId;
	}

	public void setWaitingId(Integer waitingId) {
		this.waitingId = waitingId;
	}
	
}
