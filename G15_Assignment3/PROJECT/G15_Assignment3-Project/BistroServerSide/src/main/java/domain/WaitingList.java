package domain;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import dataLayer.Visit;

import dataLayer.Guest;

/**
 * Represents the restaurant waiting list.
 * <p>
 * This class manages walk-in customers who arrive without a reservation.
 * It follows the Singleton design pattern and maintains a FIFO queue
 * of waiting parties.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Adding and removing guests from the waiting list</li>
 *   <li>Notifying the next party when a table is available</li>
 *   <li>Validating arrival times and handling late arrivals</li>
 *   <li>Providing waiting list data to the client</li>
 * </ul>
 */
public final class WaitingList {  
	
	/**
	 * Singleton instance of the waiting list.
	 */
	private static final WaitingList INSTANCE = new WaitingList();
	
	/**
	 * Represents a party waiting in the queue.
	 *
	 * @param waitingId unique verification code
	 * @param guest the guest information
	 * @param partySize number of people in the party
	 * @param readyAt timestamp when the party was notified
	 */
	public record Party(Integer waitingId, Guest guest, Integer partySize, Instant readyAt) {} 
	
	/**
	 * FIFO queue representing the waiting list.
	 */
	private final LinkedList <Party> queue= new LinkedList<>();
	
	/**
	 * Private constructor to prevent external instantiation.
	 */
	private WaitingList() {}
	
	/**
	 * Next unique waiting ID to be assigned.
	 */
	private int nextId = 1;
	
	/**
	 * Adds a guest to the waiting list.
	 * <p>
	 * The guest must provide at least one contact detail (phone or email),
	 * and cannot already exist in the waiting list.
	 *
	 * @param guest the arriving {@link Guest}
	 * @param partySize number of people in the party
	 * @return a unique waiting ID (verification code)
	 * @throws IllegalArgumentException if input validation fails
	 */
	public synchronized int join (Guest guest, Integer partySize) { 
		
		if(guest == null) throw new IllegalArgumentException("guest is null");
		if(partySize <= 0) throw new IllegalArgumentException("party sizee must be at least 1");
		
		boolean hasPhone = false , hasEmail = false;
		if(guest.getPhoneNumber() != null && !guest.getPhoneNumber().isBlank()) { hasPhone = true;} // not empty and not null
		if(guest.getEmail() != null && !guest.getEmail().isBlank()) { hasEmail = true;}
		if (!hasPhone && !hasEmail) { 							  // if both are empty or null
            throw new IllegalArgumentException("Must provide phone and/or email");
		}
		if(isGuestInQueue(guest.getPhoneNumber(),guest.getEmail())) {
			throw new IllegalArgumentException("Customer already in waiting list");
		}
		Integer waitingId = nextId++;
        queue.addLast(new Party(waitingId, guest, partySize, null)); // add to end of queue 
        return waitingId;

	}
	
	/**
	 * Returns the singleton instance of the waiting list.
	 *
	 * @return the {@link WaitingList} instance
	 */
	public static WaitingList getInstance() {
		return INSTANCE; 
	}
	
	/**
	 * Returns the next party in the queue without removing it.
	 *
	 * @return the next {@link Party} or {@code null} if the queue is empty
	 */
	public synchronized Party showParty() {
		return queue.peekFirst();
	}
	
	/**
	 * Removes and returns the next party from the queue.
	 *
	 * @return the removed {@link Party} or {@code null} if empty
	 */
	 public synchronized Party pop() {
        return queue.pollFirst();
    }
	
	/**
	 * Clears the entire waiting list.
	 */
	public synchronized void clear() {
	        queue.clear();
	}
	
	/**
	 * Removes a party from the waiting list using its verification code.
	 *
	 * @param waitingId the verification code
	 * @return {@code true} if the party was removed successfully
	 */
	 public synchronized boolean exitGuestFromList(int waitingId) {
		 for(int i = 0; i < queue.size(); i++) {
			 if(queue.get(i).waitingId == waitingId) {
				 queue.remove(i);
				 return true;
			 }
		 }
		 return false;
	 }
	 
	/**
	 * Notifies the next party in the queue that a table is available.
	 * <p>
	 * Sets the notification timestamp if the party has not already
	 * been notified.
	 *
	 * @return the {@link Guest} of the notified party,
	 * or {@code null} if the queue is empty
	 */
	 public synchronized Guest notifyPartyHead() {
		 Party head = queue.peekFirst();
		 if (head == null) {return null;}
		 
		 if (head.readyAt() != null) return head.guest; // if the party was already notified
		 //returns party with the time and date of this method execution for notification time
		 Party updatedParty = new Party(head.waitingId, head.guest, head.partySize, Instant.now());
		 queue.set(0, updatedParty); 
		 return head.guest;
	 }

	 /**
	 * Confirms the arrival of a party using its verification code.
	 * <p>
	 * The arrival must occur within the allowed late time limit.
	 *
	 * @param waitingId the verification code
	 * @param maxLateMinutes maximum allowed lateness
	 * @return the {@link Guest} if arrival is valid,
	 *         or {@code null} otherwise
	 */
	 public synchronized Guest confirmArrival(int waitingId, int maxLateMinutes){ 
		 
		 removeExpiredTimeParty(maxLateMinutes);
		 for(int i = 0; i < queue.size(); i++) {
			Party party = queue.get(i); 
			if(party.waitingId() == waitingId) { 
				if(party.readyAt == null) { return null;} // has not been notified before confirming arrival
				long timePassed = Duration.between(party.readyAt(), Instant.now()).toMinutes();
				if (timePassed >= maxLateMinutes) { // reject party for being too late for order time
                    queue.remove(i);
                    return null;
				}
				queue.remove(i); // party is on time
                return party.guest;
		  }	
		} 
		 return null;
	 }
	 	
	 /**
	 * Removes all parties that exceeded the maximum allowed waiting time.
	 *
	 * @param maxLateMinutes maximum allowed lateness in minutes
	 */
	 public synchronized void removeExpiredTimeParty(int maxLateMinutes) { // removes for the queue any party that has exceeded the time limit in waiting list
		 Instant now = Instant.now();
		 Party party = null;
		 for(int i = 1; i < queue.size(); i ++) {
			 party = queue.get(queue.size() - i); // in descending order because removing elements shifts indexes
			 if(party.readyAt == null) {break;}
			 long minutesPassed = Duration.between(party.readyAt(), now).toMinutes(); // calculates time difference
			 if (minutesPassed >= maxLateMinutes) { 
	                queue.remove(i);
			 }
		 }
	 }
	 
	/**
	 * Checks whether a guest already exists in the waiting list.
	 *
	 * @param phone guest phone number
	 * @param email guest email address
	 * @return {@code true} if the guest is already in the queue
	 */
	 private boolean isGuestInQueue(String phone, String email) {
		boolean hasPhone = false , hasEmail = false;
		if(phone != null && !phone.isBlank()) { hasPhone = true;} // not empty and not null
		if(email != null && !email.isBlank()) { hasEmail = true;}
		for(int i = 0; i < queue.size(); i++) {
			Party party = queue.get(i);
			if(hasPhone && party.guest.getPhoneNumber() != null && party.guest.getPhoneNumber().equals(phone)) {return true;} // we check both because it's only mandatory to have one
			if(hasEmail && party.guest.getEmail() != null && party.guest.getEmail().equals(email)) {return true;}
		}
		return false;
	 }
	 
	/**
	 * Converts the waiting list to a list of {@link Visit} objects.
	 * <p>
	 * Used for transferring waiting list data to the client side.
	 *
	 * @return list of {@link Visit} representations of the waiting list
	 */
	 public synchronized List<Visit> getWaitingListAsVisits() {
		 List<Visit> visits = new ArrayList<>();
        for (Party party : queue) {
            // Convert internal Party object to shared Visit object
            Visit v = new Visit(party.guest(), null); //'party.guest()' the client familiar with, and 'null' for the date because its irrelevant for the queue right now
            
            // adding the last details that the Visit object dont have
            v.setVisitId(party.waitingId()); 
            v.setPartySize(party.partySize());
            visits.add(v);
        }
        return visits;
    }	 
	 	 
}
