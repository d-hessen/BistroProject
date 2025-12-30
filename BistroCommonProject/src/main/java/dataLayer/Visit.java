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
	
	public Visit(Reservation reservation, Table table) {
		if(reservation != null) {
			this.guest = reservation.getGuest();
			setTable(table);		}
		else {
			System.err.println("Use another constructor Visit(Guest)");
		}
	}
	
	public Visit(Guest guest, Table table) {
		this.guest = guest;
		setTable(table);
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

	private void setActive(boolean isActive) {
		if(isActive && !this.isActive) {
			startVisit();
		}
		else {
			if(this.isActive) {
				endVisit();
			}
		}
		this.isActive = isActive;
	}
	
	protected void startVisit() {
		if(startTime == null) {
			LocalDate currentDate = LocalDate.now();
			LocalTime currentTime = LocalTime.now();
			startTime = new DateTime(currentDate.toString(), currentTime.toString());
		}
	}
	
	protected void endVisit() {
		if(startTime != null) {
			LocalDate currentDate = LocalDate.now();
			LocalTime currentTime = LocalTime.now();
			endTime = new DateTime(currentDate.toString(), currentTime.toString());
		}
		else {
			System.err.println("Error trying end visit that doesn't have start time!!");
		}

	}

}
