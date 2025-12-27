package domain;

import java.util.LinkedList;
import java.util.Queue;

import dataLayer.Guest;

public final class WaitingList {
	
	
	public record Party (int id, Guest guest, int partySize) {} // creates small class with fields and getters
	private int nextId = 1;
	private final Queue <Party> queue= new LinkedList<>(); // Queue that represents the Waiting List
	
	public synchronized int join (Guest guest, int partySize) { // joining the queue one at a time
		
		if(guest == null) throw new IllegalArgumentException("guest is null");
		if(partySize <= 0) throw new IllegalArgumentException("party sizee must be at least 1");
		
		int waitingId = nextId++;
		queue.add(new Party(waitingId, guest, partySize));
		return waitingId;
	}
	
	public synchronized Party showParty() { // shows the next party in the queue
		return queue.peek();
	}
	
	 public synchronized Party pop() { // returns next party and removes it from the queue
        return queue.poll();
    }
	 
	 public synchronized void clear() { // clears entire queue
	        queue.clear();
	    }
}
