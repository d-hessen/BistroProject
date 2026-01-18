package dataLayer;

import java.io.Serializable;
import java.time.LocalDateTime;
import common.Status;

/**
 * Represents a reservation made by a guest or registered member.
 * Contains reservation details, status, and guest information.
 */
public class Reservation implements Serializable {

	/** Unique reservation identifier. */
    private Integer reservationId;

    /** Member ID if the reservation belongs to a registered member. */
    private Integer memberId;

    /** Number of guests for the reservation. */
    private Integer numberOfGuests;

    /** Verification code used for identification. */
    private String verificationCode;

    /** Date and time of the reservation. */
    private DateTime reservationDate;

    /** Date when the reservation was placed. */
    private String dateOfPlacingReservation;

    /** Current reservation status. */
    private Status status;

    /** Guest details associated with the reservation. */
    private Guest guest; 
    
    /**
     * Creates a new reservation to be stored in the database.
     *
     * @param reservationDate date and time of the reservation
     * @param numberOfGuests  number of guests
     * @param memberId        member ID (nullable)
     * @param guest           guest information
     */
    public Reservation(DateTime reservationDate, Integer numberOfGuests, Integer memberId, Guest guest) {
        this.reservationId = null; // ID will be assigned by the DB
        this.reservationDate = reservationDate;
        this.numberOfGuests = numberOfGuests;
        this.memberId = memberId;
        this.guest = guest;
        
        // Set creation time and generate verification code automatically
        setDateOfPlacingReservation(null);       
        setStatus(null); // Default status will be pending
    }

    /**
     * Creates a reservation instance retrieved from the database.
     *
     * @param reservationId   reservation ID
     * @param reservationDate date and time of the reservation
     * @param verificationCode verification code
     * @param numberOfGuests  number of guests
     * @param memberId        member ID
     * @param guest           guest information
     */
    public Reservation(Integer reservationId, DateTime reservationDate,String verificationCode, Integer numberOfGuests, Integer memberId, Guest guest) {
        // Call the first constructor
        this(reservationDate, numberOfGuests, memberId, guest);
        // Set the ID from the database
        this.reservationId = reservationId; 
        this.verificationCode = verificationCode; 
    }
    
    /**
     * Creates a lightweight reservation instance.
     *
     * @param reservationId reservation ID
     * @param numberOfGuests number of guests
     * @param verificationCode verification code
     */
    public Reservation(Integer reservationId,Integer numberOfGuests, String verificationCode) {
		this.numberOfGuests = numberOfGuests;
		setReservationId(reservationId);
		setVerificationCode(verificationCode);
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
    
    /** @return reservation ID */
    public Integer getReservationId() { 
    		return reservationId; 
    }
    
    /** @param reservationId reservation ID */
    public void setReservationId(Integer reservationId) { 
    		this.reservationId = reservationId; 
    }
    
    /** @return member ID */
    public Integer getMemberId() { 
    		return memberId; 
    }
    
    /** @param memberId member ID */
    public void setMemberId(Integer memberId) { 
    		this.memberId = memberId; 
    }

    /** @return number of guests */
    public Integer getNumberOfGuests() { 
    		return numberOfGuests; 
    }
    
    /** @param numberOfGuests number of guests */
    public void setNumberOfGuests(Integer numberOfGuests) { 
    	this.numberOfGuests = numberOfGuests; 
    }

    /** @return verification code */
    public String getVerificationCode() { 
    		return verificationCode; 
    }
    
    /** @param verificationCode verification code */
    public void setVerificationCode(String verificationCode) { 
    		this.verificationCode = verificationCode; 
    }
    
    /** @return reservation date and time */
    public DateTime getReservationDate() { 
    		return reservationDate; 
    }
    
    /** @param reservationDate reservation date and time */
    public void setReservationDate(DateTime reservationDate) { 
    		this.reservationDate = reservationDate; 
    }
    
    /** @return reservation status */
    public Status getStatus() { 
    		return status; 
    }
    
    /** @param status reservation status */
    public void setStatus(Status status) {
        // if null -> pending
        this.status = (status != null) ? status : Status.pending;
    }
    
    /** @return guest information */
    public Guest getGuest() { 
    		return guest;    	
    }
    
    /** @param guest guest information */
    public void setGuest(Guest guest) { 
    		this.guest = guest; 
    }
}