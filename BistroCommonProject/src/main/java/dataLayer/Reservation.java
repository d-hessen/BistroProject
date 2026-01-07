package dataLayer;

import java.io.Serializable;
import java.time.LocalDateTime;
import common.Status;

public class Reservation implements Serializable {
    private Integer reservationId; 
    private Integer memberId; 
    private Integer numberOfGuests;
    private String verificationCode; 
    private DateTime reservationDate;
    private String dateOfPlacingReservation;
    private Status status; 
    private Guest guest; 
    
    // ==========================================
    // Constructor 1: For creating a new reservation
    // The Database will generate the ID automatically.
    // ==========================================
    public Reservation(DateTime reservationDate, Integer numberOfGuests, Integer memberId, Guest guest) {
        this.reservationId = null; // ID will be assigned by the DB
        this.reservationDate = reservationDate;
        this.numberOfGuests = numberOfGuests;
        this.memberId = memberId;
        this.guest = guest;
        
        // Set creation time and generate verification code automatically
        setDateOfPlacingReservation(null); 
        generateVerificationCode();        
        setStatus(null); // Default status will be pending
    }

    // ==========================================
    // Constructor 2: For retrieving an existing reservation
    // Used when fetching data from the DB.
    // ==========================================
    public Reservation(Integer reservationId, DateTime reservationDate, Integer numberOfGuests, Integer memberId, Guest guest) {
        // Call the first constructor
        this(reservationDate, numberOfGuests, memberId, guest);
        // Set the ID from the database
        this.reservationId = reservationId; 
    }

    // generates a random 4-digit verification code
    private void generateVerificationCode() {
    	this.verificationCode = String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    // getter and setter for Date of Placing
    public String getDateOfPlacingReservation() {
        return dateOfPlacingReservation;
    }

    public void setDateOfPlacingReservation(String dateOfPlacingReservation) {
        if(dateOfPlacingReservation == null) {
            // Set to current server time if null
            LocalDateTime currentDateTime = LocalDateTime.now();
            this.dateOfPlacingReservation = currentDateTime.toString();
        } else {
            this.dateOfPlacingReservation = dateOfPlacingReservation;
        }
    }
    
    public Reservation(Integer reservationId,Integer numberOfGuests, String verificationCode) {
		this.numberOfGuests = numberOfGuests;
		setReservationId(reservationId);
		setVerificationCode(verificationCode);

	}
    
    // getters and setters for the other fields

    public Integer getReservationId() { 
    	return reservationId; 
    }
    public void setReservationId(Integer reservationId) { 
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
    public void setVerificationCode(String verificationCode) { 
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
        // if null -> pending
        this.status = (status != null) ? status : Status.pending;
    }
    public Guest getGuest() { 
    	return guest;    	
    }
    public void setGuest(Guest guest) { 
    	this.guest = guest; 
    }
}