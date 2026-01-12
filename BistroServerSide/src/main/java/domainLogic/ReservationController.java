package domainLogic;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.*;
import java.util.Random;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import common.Action;
import common.BistroMessage;
import common.Status;
import dataLayer.DateTime;
import dataLayer.Reservation;
import dataLayer.Visit;
import dataLayer.Table;
import databaseController.CreateCommands;
import databaseController.DeleteCommands;
import databaseController.GetCommands;
import databaseController.UpdateCommands;

// RESERVATION LOGIC 
public class ReservationController {
	private static final int TIME_SLOT = 2;
	public static BistroMessage getReservation(Integer reservationID, ServerFrameController guiController) {
		Reservation recieved = GetCommands.getReservation(reservationID, guiController);
		if(recieved != null) {
			return new BistroMessage(Action.GET_RESERVATION, recieved);
		}
		return new BistroMessage(Action.RESERVATION_NOT_FOUND, recieved);
	}
	
	public static BistroMessage updateReservation(Reservation reservationToUpdate, ServerFrameController guiController) {
		boolean success = UpdateCommands.updateReservation(reservationToUpdate, guiController);
		return new BistroMessage(Action.UPDATE_RESERVATION, success);
	}
	
	public static BistroMessage checkAvailability(Reservation Reservation, ServerFrameController guiController) {
		List<String> validTimes = new ArrayList<>();
		List<Table> tables = GetCommands.getAllActiveTables(guiController);
		
	    final int TIME_SLOT = 2; // Duration
		List<String> allTimeSlots = Arrays.asList(
			    "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", 
			    "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", 
			    "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", 
			    "21:00", "21:30", "22:00", "22:30", "23:00");
		
		Reservation.setStatus(Status.pending);
		for(String time :allTimeSlots) {
			
			// get the day of the reservation and create
			DateTime today = new DateTime(Reservation.getReservationDate().getDate(), time);
			LocalDateTime start = changeToLocalDateTime(today);
	        if (start == null) continue;
	        LocalDateTime windowStart = start.minusHours(TIME_SLOT);
	        LocalDateTime windowEnd = start.plusHours(TIME_SLOT);
	        
	        // reservation overlap for a specific timeSlot for today
	        List<Reservation> overlap = GetCommands.getReservationOverlap(windowStart, windowEnd, guiController);
	        Reservation stubReservation = new Reservation(today, Reservation.getNumberOfGuests(), Reservation.getMemberId(), null);
	        
	        // run algorithm
	        Integer result = findBestArrangementAndWaste(tables, overlap, stubReservation, TIME_SLOT);
	        if (result != null) { // result = null means there is no possible arrangement
	        	validTimes.add(time);
	        }
	    }
		return new BistroMessage(Action.CHECK_RESERVATION_AVAILABILITY, validTimes);	
	}

	public static BistroMessage createReservation(Reservation reservationToCreate, ServerFrameController guiController) {
     
	    try {
	        //  validate data
	        if (reservationToCreate == null || reservationToCreate.getReservationDate() == null) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Missing reservation date/time.");
	        }
	        if (reservationToCreate.getNumberOfGuests() == null || reservationToCreate.getNumberOfGuests() <= 0) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Invalid number of guests.");
	        }
	        if (reservationToCreate.getMemberId() == null && reservationToCreate.getGuest() == null) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Missing customer details (member or guest).");
	        }

	        //  availability check with findBestArrangementAndWaste algorithm
	       
	        LocalDateTime start = changeToLocalDateTime(reservationToCreate.getReservationDate());
	        List<Table> tables = GetCommands.getAllActiveTables(guiController);
	        List<Reservation> overlap = GetCommands.getReservationOverlap(start.minusHours(TIME_SLOT), start.plusHours(TIME_SLOT), guiController);

	        Integer arrangement = findBestArrangementAndWaste(tables, overlap, reservationToCreate, TIME_SLOT);
	        if (arrangement == null) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "No available table for reservation");
	        }

	        //  set fields before insert 
	        reservationToCreate.setVerificationCode(generateVerificationCode());
	        reservationToCreate.setStatus(Status.approved); 
	        reservationToCreate.setDateOfPlacingReservation(null); // sets "now" if null

	        // insert to DB
	        Reservation created = CreateCommands.createReservation(reservationToCreate, guiController);
	        if (created == null) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "DB insert failed. Check server console.");
	        }
	        System.out.println("yes");
	        return new BistroMessage(Action.CREATE_RESERVATION, created);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Server error: " + e.getMessage());
	    }
	}

	
	public static BistroMessage cancelReservation(Reservation res, ServerFrameController guiController) {	    
	    boolean success = DeleteCommands.deleteReservation(res.getReservationId(), guiController);    
	    if (success) {
	        return new BistroMessage(Action.CANCEL_RESERVATION, true);
	    } else {
	        return new BistroMessage(Action.CANCEL_RESERVATION, false);
	    }
	}
	
	public static BistroMessage codeVerification(String code, ServerFrameController guiController) {
		Reservation reservation = GetCommands.getReservationVerificationCode(code, guiController);
		if(reservation != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, reservation);
		}
		Visit waiting = GetCommands.getWaitingVisit(code, guiController);
		if(waiting != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, waiting);
		}
		return new BistroMessage(Action.GET_VERIFICATION_CODE, "Error: Verification Code wasn't found");
	}

	public static String generateVerificationCode() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		String result = random.ints(5, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
		return result;
	}

	public static BistroMessage getMemberReservations(String phoneNumber, ServerFrameController guiController) {
		List<Reservation> memberReservations = new ArrayList<>();
		memberReservations = GetCommands.getReservationsByPhoneNumber(phoneNumber, guiController); 
	    if (memberReservations != null) {
	        return new BistroMessage(Action.GET_MEMBER_RESERVATIONS, memberReservations);
	    } else {
	        return new BistroMessage(Action.MEMBER_NOT_FOUND, phoneNumber);
	    }
	}
	

	
	// changes class DateTime to LocalDateTime
	private static LocalDateTime changeToLocalDateTime(DateTime dt) {
		if (dt == null || dt.getDate() == null || dt.getTime() == null) return null;
	    
	    try {
	        LocalDate date = LocalDate.parse(dt.getDate()); // Expects yyyy-MM-dd
	        LocalTime time;
	        try {
	            time = LocalTime.parse(dt.getTime());
	        } catch (DateTimeParseException e) {
	            try {
	                 time = LocalTime.parse(dt.getTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
	            } catch (Exception e2) {
	                 String timeStr = dt.getTime();
	                 if (timeStr.length() >= 5) {
	                     time = LocalTime.parse(timeStr.substring(0, 5));
	                 } else {
	                     throw e2;
	                 }
	            }
	        }
	        
	        return LocalDateTime.of(date, time);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	//generates random 10 characters code
	private static String generateCode() {
		
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();

		String result = random.ints(10, 0, chars.length()).mapToObj(chars::charAt).map(Object::toString).collect(Collectors.joining());
		System.out.println(result);
		return result;
		}
		
	
	private static Integer findBestArrangementAndWaste(List<Table> tables, List<Reservation> overlap ,Reservation newRes, int timeSlot) {
		//run algorithm twice, check check largest party amounts first
		Integer bigFirst = runGreedyReturnTableAndWaste(tables, overlap, newRes, timeSlot, true);
		Integer smallFirst = runGreedyReturnTableAndWaste(tables, overlap, newRes, timeSlot, false);
		
		// if none succeed
		if (bigFirst == null && smallFirst == null) return null;
		// if only one succeeds
		if (bigFirst != null && smallFirst == null) return bigFirst;
		if (bigFirst == null && smallFirst != null) return smallFirst;
		// both succeeded we pick smaller waste
		if(smallFirst < bigFirst) {
			return smallFirst;
		}
		return bigFirst;
	}
	
	
	private static Integer runGreedyReturnTableAndWaste(List<Table> tables, List<Reservation> overlap, Reservation newRes, int timeSlot, boolean isBigFirst) {

		// sort all tables by table capacity
		List<Table> tablesSorted = new ArrayList<>(tables);
		tablesSorted.sort(Comparator.comparingInt(Table::getTableCapacity));

		// map with key of table number and the latest time it's busy until
		Map<Integer, LocalDateTime> busyUntil = new HashMap<>();
		for (Table table : tablesSorted) {
			busyUntil.put(table.getTableNumber(), LocalDateTime.MIN);
		}
		// Combine reservations
		List<Reservation> all = new ArrayList<>(overlap.size() + 1);
		for (Reservation res : overlap) {
			if (res.getStatus() == Status.cancelled || res.getStatus() == Status.no_show ) continue; 
			all.add(res);
		}
		all.add(newRes);

		// Comparator to sort all overlapping reservations by time
		Comparator<Reservation> byTime = Comparator.comparing(r -> changeToLocalDateTime(r.getReservationDate()));
		// Comparator to sort all overlapping reservations by number of guests
		Comparator<Reservation> byGuests = Comparator.comparingInt(Reservation::getNumberOfGuests);
		
		if (isBigFirst) byGuests = byGuests.reversed(); // reverses the order according to this comparator
		all.sort(byTime.thenComparing(byGuests)); // first sort by time then by guest amount
		Integer newTableNumber = null; // new assigned table 
		int totalWaste = 0;

		for (Reservation res : all) { 
			LocalDateTime start = changeToLocalDateTime(res.getReservationDate());
			if (start == null) return null;
			int guests = res.getNumberOfGuests();
			Table chosen = null;
			
			for (Table table : tablesSorted) {
				LocalDateTime busyUntilTime = busyUntil.get(table.getTableNumber());
				boolean free = !busyUntilTime.isAfter(start); // is the table still busy by the time the reservation starts?
				boolean canFit = (table.getTableCapacity() >= guests);

				if (free && canFit) {
					chosen = table;
					break;
				}
			}
			if (chosen == null) return null;
			if (res == newRes) newTableNumber = chosen.getTableNumber();
			
			// for every reservation i want to add the total amount of seats waisted
			totalWaste += (chosen.getTableCapacity() - guests); 
			// locking table for 2 hours starting from out start time
			busyUntil.put(chosen.getTableNumber(), start.plusHours(timeSlot));
		}
		
		if (newTableNumber == null) return null; // fails if there is no new reservation
	
		return  totalWaste;
	}
	
	
	

}