package domain;

import java.time.LocalTime;

import dataLayer.DateTime;
import dataLayer.Guest;

/**
 * Represents the restaurant domain entity.
 * <p>
 * This class follows the Singleton design pattern and models
 * the core restaurant configuration and waiting list management.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Managing opening and closing times</li>
 *   <li>Defining reservation duration and late arrival policy</li>
 *   <li>Handling the waiting list lifecycle</li>
 * </ul>
 */
public final class Restaurant {
	
	/**
	 * The single instance of the Restaurant (Singleton).
	 */
	private static final Restaurant INSTANCE = new Restaurant();
	
	/** Unique identifier of the restaurant. */
	private int restaurantId;

	/** Restaurant opening time. */
    private DateTime openTime;

    /** Restaurant closing time. */
    private DateTime closeTime;

    /** Default reservation duration in hours. */
    private int reservationDurationHours;

    /** Maximum allowed lateness in minutes. */
    private int maxLateMinutes;
    
    /**
	 * Singleton waiting list instance associated with the restaurant.
	 */
	private final WaitingList waitingList = WaitingList.getInstance();
	
	/**
	 * Returns the singleton instance of the restaurant.
	 *
	 * @return the {@link Restaurant} instance
	 */
	public static Restaurant getInstance() {return INSTANCE;} // if not null?
	
	/**
	 * Private constructor to prevent external instantiation.
	 * Initializes default restaurant configuration values.
	 */
	private Restaurant() { 
		this.restaurantId = 0;
	    this.openTime = new DateTime(null, "12:00");
	    this.closeTime = new DateTime(null, "23:00");
	    this.reservationDurationHours = 2;
	    this.maxLateMinutes = 15;
	    }
	
	/**
	 * Returns the restaurant waiting list.
	 *
	 * @return the {@link WaitingList} instance
	 */
	public WaitingList getWaitingList() {return waitingList; }
	
	/**
	 * Returns the restaurant identifier.
	 *
	 * @return restaurant ID
	 */
	public int getRestaurantId() {return restaurantId;}
	
	/**
	 * Returns the restaurant opening time.
	 *
	 * @return opening {@link DateTime}
	 */
	public DateTime getOpenTime() {return openTime;}
	
	/**
	 * Returns the restaurant closing time.
	 *
	 * @return closing {@link DateTime}
	 */
	public DateTime getCloseTime() {return closeTime;}
	
	/**
	 * Returns the reservation duration in hours.
	 *
	 * @return reservation duration
	 */
	public int getReservationDurationHours() {return reservationDurationHours;}
	
	/**
	 * Returns the maximum allowed lateness in minutes.
	 *
	 * @return maximum late minutes
	 */
	public int getMaxLateMinutes() {return maxLateMinutes;}
	
	/**
	 * Sets the restaurant opening time.
	 *
	 * @param openTime new opening {@link DateTime}
	 */
	public void setOpenTime(DateTime openTime) {
		this.openTime = openTime;
	}

	/**
	 * Sets the restaurant closing time.
	 *
	 * @param closeTime new closing {@link DateTime}
	 */
	public void setCloseTime(DateTime closeTime) {
		this.closeTime = closeTime;
	}

	/**
	 * Sets the reservation duration in hours.
	 *
	 * @param reservationDurationHours reservation duration
	 */
	public void setReservationDurationHours(int reservationDurationHours) {
		this.reservationDurationHours = reservationDurationHours;
	}

	/**
	 * Sets the maximum allowed lateness in minutes.
	 *
	 * @param maxLateMinutes maximum late arrival minutes
	 */
	public void setMaxLateMinutes(int maxLateMinutes) {
		this.maxLateMinutes = maxLateMinutes;
	}
	
	/**
	 * Adds a guest to the waiting list.
	 * <p>
	 * Used when a customer arrives without a reservation.
	 *
	 * @param guest the arriving {@link Guest}
	 * @param partySize number of people in the party
	 * @return verification code assigned to the waiting guest
	 */
	public int joinWaitingList(Guest guest, int partySize) { // customer arrives with no reservation
		return waitingList.join(guest, partySize);
	    }
	
	/**
	 * Removes a guest from the waiting list.
	 *
	 * @param Code the guest verification code
	 * @return {@code true} if the guest was removed successfully
	 */
	public boolean cancelWaiting(int Code) { // customer can leave waiting list at any time
        return waitingList.exitGuestFromList(Code);
    }
	
	/**
	 * Retrieves the next eligible party from the waiting list.
	 * <p>
	 * Expired parties (late arrivals) are removed before selection.
	 *
	 * @return the {@link Guest} representing the next party head,
	 *         or {@code null} if no party is available
	 */
	public Guest callNextParty(){  // called when the restaurant has an available table for the next Party in waitingList
		waitingList.removeExpiredTimeParty(maxLateMinutes);
		return waitingList.notifyPartyHead();
		}
	
	/**
	 * Confirms the arrival of a party when a table becomes available.
	 *
	 * @param verificationCode the guest verification code
	 * @return the {@link Guest} if arrival is confirmed,
	 *         or {@code null} if confirmation fails
	 */
	public Guest confirmPartyArrival(int verificationCode){ // called when after a table is ready and a customer is ready to receive it
		return waitingList.confirmArrival(verificationCode, maxLateMinutes);
	}
	
	
	
	
}
