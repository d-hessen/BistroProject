package dataLayer;

import java.io.Serializable;
import java.time.LocalDateTime;

import common.Status;

public class Reservation implements Serializable {
	private Integer reservationId; 
	private Integer memberId; 
	private Integer numberOfGuests; //Can be updated
	private String verificationCode; 
	private DateTime reservationDate;
	private String dateOfPlacingReservation;
	private Status status; //Can be updated
	private Guest guest; //for fields fullName, phone, email
	
	public Reservation(Integer reservationId, DateTime reservationDate, Integer numberOfGuests, Integer memberId, Guest guest) {
		this.reservationDate = reservationDate;
		this.numberOfGuests = numberOfGuests;
		this.memberId = memberId;
		this.guest = guest;
		setDateOfPlacingReservation(dateOfPlacingReservation);
		setReservationId(reservationId);
		setVerificationCode(verificationCode);
		setStatus(status);
	}
	
	public Reservation(Integer reservationId,Integer numberOfGuests, String verificationCode) {
		this.numberOfGuests = numberOfGuests;
		setReservationId(reservationId);
		setVerificationCode(verificationCode);

	}

	public String getDateOfPlacingReservation() {
		return dateOfPlacingReservation;
	}

	public void setDateOfPlacingReservation(String dateOfPlacingReservation) {
		if(dateOfPlacingReservation == null) {
			LocalDateTime currentDateTime = LocalDateTime.now();
			this.dateOfPlacingReservation = currentDateTime.toString();
		}
		this.dateOfPlacingReservation = dateOfPlacingReservation;
	}

	public Integer getReservationId() {
		return reservationId;
	}

	private void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Integer getNumberOfGuests() {
		return numberOfGuests;
	}

	public void setNumberOfGuests(Integer numberOfGuests) {
		this.numberOfGuests = numberOfGuests;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	private void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public DateTime getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(DateTime reservationDate) {
		this.reservationDate = reservationDate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if(status != null) {
			this.status = status;
		}
		
	}

	public Guest getGuest() {
		return guest;
	}

	public void setGuest(Guest guest) {
		this.guest = guest;
	}
	
	
}
