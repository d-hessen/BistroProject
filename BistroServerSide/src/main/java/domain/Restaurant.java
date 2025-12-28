package domain;

import java.time.LocalTime;

import dataLayer.Guest;

public final class Restaurant {
	
	private static final Restaurant INSTANCE = new Restaurant(); // single instance of restaurant
	
	private int restaurantId;
    private LocalTime openTime; 
    private LocalTime closeTime;
    private int reservationDurationHours;
    private int maxLateMinutes;

	private final WaitingList waitingList = WaitingList.getInstance();  // field waiting list
	
	public static Restaurant getInstance() {return INSTANCE;}
	
	private Restaurant() {  						// private constructor prevents making new restaurants 
		this.restaurantId = 0;
	    this.openTime = LocalTime.of(12, 0);
	    this.closeTime = LocalTime.of(23, 0);
	    this.reservationDurationHours = 2;
	    this.maxLateMinutes = 15;
	    }
	
	public WaitingList getWaitingList() {return waitingList; }
	public int getRestaurantId() {return restaurantId;}
	public LocalTime getOpenTime() {return openTime;}
	public LocalTime getCloseTime() {return closeTime;}
	public int getReservationDurationHours() {return reservationDurationHours;}
	public int getMaxLateMinutes() {return maxLateMinutes;}
	
	public void setOpenTime(LocalTime openTime) {
		this.openTime = openTime;
	}

	public void setCloseTime(LocalTime closeTime) {
		this.closeTime = closeTime;
	}

	public void setReservationDurationHours(int reservationDurationHours) {
		this.reservationDurationHours = reservationDurationHours;
	}

	public void setMaxLateMinutes(int maxLateMinutes) {
		this.maxLateMinutes = maxLateMinutes;
	}
	
	
	public int joinWaitingList(Guest guest, int partySize, String phone, String email) { // customer arrives with no reservation
		return waitingList.join(guest, partySize, phone, email);
	    }
	
	public boolean cancelWaiting(int Code) { // customer can leave waiting list at any time
        return waitingList.exitGuestFromList(Code);
    }
	
	public WaitingList.Party callNextParty(){  // called when the restaurant has an available table for the next Party in waitingList
		waitingList.removeExpiredTimeParty(maxLateMinutes);
		return waitingList.notifyPartyHead();
		}
	
	public WaitingList.Party confirmPartyArrival(int verificationCode){ // called when after a table is ready and a customer is ready to receive it
		return waitingList.confirmArrival(verificationCode, maxLateMinutes);
	}
	
	
	
	
}
