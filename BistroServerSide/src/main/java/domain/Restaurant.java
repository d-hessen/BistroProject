package domain;

import java.time.LocalTime;

import dataLayer.Guest;

public final class Restaurant {
	
	private static final Restaurant INSTANCE = new Restaurant();
	
	private int restaurantId;
    private LocalTime openTime; 
    private LocalTime closeTime;
    private int reservationDurationHours;
    private int maxLateMinutes;

	private final WaitingList waitingList = new WaitingList();
	
	 public WaitingList getWaitingList() {
	        return waitingList;
	    }
	 public int joinWaitingList(Guest guest, int partySize) {
	        return waitingList.join(guest, partySize);
	    }
	
	 private Restaurant() {  						// private constructor prevents making new restaurants 
	        this.restaurantId = 0;
	        this.openTime = LocalTime.of(12, 0);
	        this.closeTime = LocalTime.of(23, 0);
	        this.reservationDurationHours = 2;
	        this.maxLateMinutes = 15;
	    }
	 
	 public static Restaurant getInstance() {
	        return INSTANCE;
	    }
}
