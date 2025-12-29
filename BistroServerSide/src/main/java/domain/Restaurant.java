package domain;

import java.time.LocalTime;

import dataLayer.DateTime;
import dataLayer.Guest;

public final class Restaurant {
	
	private static final Restaurant INSTANCE = new Restaurant(); // single instance of restaurant
	
	private int restaurantId;
    private DateTime openTime;  // change to date time 
    private DateTime closeTime;
    private int reservationDurationHours;
    private int maxLateMinutes;

	private final WaitingList waitingList = WaitingList.getInstance();  // field waiting list
	
	public static Restaurant getInstance() {return INSTANCE;} // if not null?
	
	private Restaurant() {// private constructor prevents making new restaurants 
		this.restaurantId = 0;
	    this.openTime = new DateTime(null, "12:00"); // datetime
	    this.closeTime = new DateTime(null, "23:00");
	    this.reservationDurationHours = 2;
	    this.maxLateMinutes = 15;
	    }
	
	public WaitingList getWaitingList() {return waitingList; }
	public int getRestaurantId() {return restaurantId;}
	public DateTime getOpenTime() {return openTime;}
	public DateTime getCloseTime() {return closeTime;}
	public int getReservationDurationHours() {return reservationDurationHours;}
	public int getMaxLateMinutes() {return maxLateMinutes;}
	
	public void setOpenTime(DateTime openTime) {
		this.openTime = openTime;
	}

	public void setCloseTime(DateTime closeTime) {
		this.closeTime = closeTime;
	}

	public void setReservationDurationHours(int reservationDurationHours) {
		this.reservationDurationHours = reservationDurationHours;
	}

	public void setMaxLateMinutes(int maxLateMinutes) {
		this.maxLateMinutes = maxLateMinutes;
	}
	
	
	public int joinWaitingList(Guest guest, int partySize) { // customer arrives with no reservation
		return waitingList.join(guest, partySize);
	    }
	
	public boolean cancelWaiting(int Code) { // customer can leave waiting list at any time
        return waitingList.exitGuestFromList(Code);
    }
	
	public Guest callNextParty(){  // called when the restaurant has an available table for the next Party in waitingList
		waitingList.removeExpiredTimeParty(maxLateMinutes);
		return waitingList.notifyPartyHead();
		}
	
	public Guest confirmPartyArrival(int verificationCode){ // called when after a table is ready and a customer is ready to receive it
		return waitingList.confirmArrival(verificationCode, maxLateMinutes);
	}
	
	
	
	
}
