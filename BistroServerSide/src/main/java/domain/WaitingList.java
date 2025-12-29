package domain;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

import dataLayer.Guest;
import domain.WaitingList.Party;

public final class WaitingList {  
	
	private static final WaitingList INSTANCE = new WaitingList();
	public record Party(int id, Guest guest, int partySize, Instant readyAt) {} // creates small class with fields and getters (id is validation code)
	private final LinkedList <Party> queue= new LinkedList<>();  // Queue that represents the Waiting List
	private WaitingList() {}
	private int nextId = 1;
	
	public synchronized int join (Guest guest, int partySize) { // joining the queue one at a time and returns unique waitingId
		
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
		int id = nextId++;
        queue.addLast(new Party(id, guest, partySize, null)); // add to end of queue 
        return id;

	}
	public static WaitingList getInstance() { // returns WaitingList INSTANCE
		return INSTANCE; 
	}
	
	public synchronized Party showParty() { // shows the next party in the queue
		return queue.peekFirst();
	}
	
	 public synchronized Party pop() { // returns next party and removes it from the queue
        return queue.pollFirst();
    }
	 
	 public synchronized void clear() { // clears entire queue
	        queue.clear();
	}
	
	 public synchronized boolean exitGuestFromList(int waitingId) { // if party wants to be removed from waitingList by verification code
		 for(int i = 0; i < queue.size(); i++) {
			 if(queue.get(i).id == waitingId) {
				 queue.remove(i);
				 return true;
			 }
		 }
		 return false;
	 }
	 
	 public synchronized Guest notifyPartyHead() { // called when a table becomes available and notifies the next party
		 Party head = queue.peekFirst();
		 if (head == null) {return null;}
		 
		 if (head.readyAt() != null) return head.guest; // if the party was already notified
		 //returns party with the time and date of this method execution for notification time
		 Party updatedParty = new Party(head.id, head.guest, head.partySize, Instant.now());
		 queue.set(0, updatedParty); 
		 return head.guest;
	 }
	// called when a customer arrives and provides their code and returns a party if arrival time is valid
	 public synchronized Guest confirmArrival(int waitingId, int maxLateMinutes){ 
		 
		 removeExpiredTimeParty(maxLateMinutes);
		 for(int i = 0; i < queue.size(); i++) {
			Party party = queue.get(i); 
			if(party.id() == waitingId) { 
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
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
}
