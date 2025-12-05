package dataLayer;

import java.io.Serializable;

public class Reservation implements Serializable {
	private Integer reservationId;
	private Integer memberId;
	private Integer numberOfGuests;
	private Integer verificationCode;
	private String dateOfPlacingReservation;
	private String reservationDate;
	
	private int reservationCounter;
	
	/**
	 * @param reservationId
	 * @param reservationDate
	 * @param numberOfGuests
	 * @param verificationCode
	 * @param dateOfPlacingReservation
	 * @param memberId
	 */
	public Reservation(Integer reservationId, String reservationDate, Integer numberOfGuests, Integer verificationCode, String dateOfPlacingReservation, Integer memberId) {
		super();
		setReservationId(reservationId);
		this.reservationDate = reservationDate;
		this.numberOfGuests = numberOfGuests;
		this.verificationCode = verificationCode;
		this.dateOfPlacingReservation = dateOfPlacingReservation;
		this.memberId = memberId;
	}
	
	public int getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationCounter++;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	public void setNumberOfGuests(int numberOfGuests) {
		this.numberOfGuests = numberOfGuests;
	}

	public int getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(int verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String getDateOfPlacingReservation() {
		return dateOfPlacingReservation;
	}

	public void setDateOfPlacingReservation(String dateOfPlacingReservation) {
		this.dateOfPlacingReservation = dateOfPlacingReservation;
	}

	public String getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(String reservationDate) {
		this.reservationDate = reservationDate;
	}

	public int getReservationCounter() {
		return reservationCounter;
	}

	public String toString(){
		return String.format("%s %s %s %s %s\n",reservationId,reservationDate,numberOfGuests,verificationCode,dateOfPlacingReservation,memberId);
	}
}
