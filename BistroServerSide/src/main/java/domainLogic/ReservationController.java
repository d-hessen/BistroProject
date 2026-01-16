package domainLogic;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import common.Action;
import common.BistroMessage;
import common.Status;
import dataLayer.DateTime;
import dataLayer.Guest;
import dataLayer.Reservation;
import dataLayer.RestaurantConfig;
import dataLayer.Visit;
import dataLayer.Table;
import databaseController.CreateCommands;
import databaseController.DeleteCommands;
import databaseController.GetCommands;
import databaseController.UpdateCommands;
import utils.EmailSend;

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
		List<Table> tables = GetCommands.getAllTablesWithStatus(guiController);
		
		
		DateTime reservationDate = Reservation.getReservationDate();
		RestaurantConfig bistroRestaurant = GetCommands.getRestaurantConfig(guiController);
		
		LocalDate dateForSpecialHours = LocalDate.parse(reservationDate.getDate());
		String dateForRegularHours = LocalDate.parse(reservationDate.getDate()).getDayOfWeek().toString();
		
		String openingHour = bistroRestaurant.getRegularHours().get(dateForRegularHours)[0];
		String closingHour = bistroRestaurant.getRegularHours().get(dateForRegularHours)[1];
		
		if(bistroRestaurant.getSpecialHours().containsKey(dateForSpecialHours)) {
			openingHour = bistroRestaurant.getSpecialHours().get(dateForSpecialHours)[0];
			closingHour = bistroRestaurant.getSpecialHours().get(dateForSpecialHours)[1];
		}
	
			List<String> allTimeSlots = generateTimeSlots(openingHour, closingHour);
		
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
	        if (LocalDateTime.now().plusHours(1).isAfter(changeToLocalDateTime(reservationToCreate.getReservationDate()))) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Reservation time must be at least one hour from now.");
	        }
	        if (LocalDateTime.now().plusMonths(1).isBefore(changeToLocalDateTime(reservationToCreate.getReservationDate()))) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Reservation date must be at most one month from now.");
	        }
	        if (reservationToCreate.getNumberOfGuests() == null || reservationToCreate.getNumberOfGuests() <= 0) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Invalid number of guests.");
	        }
	        if (reservationToCreate.getMemberId() == null && reservationToCreate.getGuest() == null) {
	            return new BistroMessage(Action.RESERVATION_NOT_CREATED, "Missing customer details (member or guest).");
	        }

	        //  availability check with findBestArrangementAndWaste algorithm
	       
	        LocalDateTime start = changeToLocalDateTime(reservationToCreate.getReservationDate());
	        List<Table> tables = GetCommands.getAllTablesWithStatus(guiController);
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
	        EmailSend.sendCancellationInform(res);
	        return new BistroMessage(Action.CANCEL_RESERVATION, true);
	    } else {
	        return new BistroMessage(Action.CANCEL_RESERVATION, false);
	    }
	}
	
	public static BistroMessage codeVerification(BistroMessage request, ServerFrameController guiController) {
		String code = (String)request.getData();
		Reservation reservation = GetCommands.getReservationVerificationCode(code, guiController);
		if(request.getAction().name().equals("FIND_RESERVATION")) {
			if(reservation != null) {
				return new BistroMessage(Action.FIND_RESERVATION, reservation);
			} else {
				return new BistroMessage(Action.FIND_RESERVATION, "Error: Haven't found any reservation with this code");
			}
		}
		if(reservation != null) {
			LocalTime now = LocalTime.now();
            LocalTime fifteenMinsBefore = now.minusMinutes(15);
            LocalTime fifteenMinsLater = now.plusMinutes(15);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); 
            String timeStr = reservation.getReservationDate().getTime();
            LocalTime reservationTime = LocalTime.parse(timeStr, timeFormatter);
            Visit checkedIn = GetCommands.getVisitByVerificationCode(code, guiController);
             
            if ((reservationTime.isAfter(fifteenMinsBefore) || reservationTime.equals(now)) && reservationTime.isBefore(fifteenMinsLater) || checkedIn != null) {
    			return new BistroMessage(Action.GET_VERIFICATION_CODE, reservation);
            } else {
            	return new BistroMessage(Action.GET_VERIFICATION_CODE, "There's no upcoming reservation in the next 15 minutes:(");
            }
		}
		Visit waiting = GetCommands.getWaitingVisit(code, guiController);
		if(waiting != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, waiting);
		}
		Visit visit = GetCommands.getVisitByVerificationCode(code, guiController);
		if(visit != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, visit);
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
	
	public static void processNoShows(ServerFrameController guiController) {
	    //Get reservations where status is 'approved' and late for 15 mins
	    List<Reservation> expired = GetCommands.getExpiredReservations(guiController);
	    
	    for (Reservation res : expired) {
	        // Change status to CANCELLED
	        res.setStatus(Status.cancelled);
	        UpdateCommands.updateReservation(res, guiController);
	        
	        //Notify user of cancellation
	        EmailSend.sendCancellationNotice(res);
	        
	        guiController.addToConsole("Auto-cancelled Reservation ID: " + res.getReservationId() + " due to no-show.");
	    }
	}
	
	public static void processReminders(ServerFrameController guiController) {
	    //Get reservations 2 hours from now
	    List<Reservation> upcoming = GetCommands.getReservationsForReminder(guiController); 
	    
	    for (Reservation res : upcoming) {
	        // Send Email
	        EmailSend.sendReminder(res); 
	        
	        // Update DB so we don't send again
	        UpdateCommands.markReminderSent(res.getReservationId(), guiController);
	        guiController.addToConsole("Reminder sent for Reservation ID: " + res.getReservationId());
	    }
	}
	
	public static BistroMessage recoverLostCode(String contactInput, ServerFrameController guiController) {
        List<Reservation> found = GetCommands.getReservationsByContactInfo(contactInput, guiController);
        
        if (found == null || found.isEmpty()) {
            return new BistroMessage(Action.FORGOT_CODE, "Error: No reservations found for these details.");
        }

        Reservation representative = found.get(0);
        String userEmail = null;
        String userPhone = null;

        if (representative.getGuest() != null) {
            userEmail = representative.getGuest().getEmail();
            userPhone = representative.getGuest().getPhoneNumber();
        }

        if (userEmail != null && !userEmail.isEmpty()) {
            
            EmailSend.sendReservationsTableByEmail(found);
            guiController.addToConsole("Recovery sent via Email to: " + userEmail);

        } 

        if (userPhone != null && !userPhone.isEmpty()) {
            
            StringBuilder smsContent = new StringBuilder();
            smsContent.append("SMS:\n");
            for (Reservation res : found) {
                smsContent.append("Code: ").append(res.getVerificationCode())
                          .append(" (Date: ").append(res.getReservationDate().getDate()).append(")\n");
            }
            
            guiController.addToConsole("Recovery sent via SMS Popup to: " + userPhone);

            return new BistroMessage(Action.FORGOT_CODE, smsContent.toString());
        }
        return new BistroMessage(Action.FORGOT_CODE, "Verification codes have been sent to your Email and SMS.");
    }
	
	public static BistroMessage getAllReservations(ServerFrameController guiController) {
	    List<Reservation> allReservations = GetCommands.getAllReservations(guiController);
	    return new BistroMessage(Action.GET_ALL_RESERVATIONS, allReservations);
	}
	
//	public static void ExistingReservationsNeedToBeCancled(ServerFrameController guiController) {
//		List<Reservation> allReservations;
//		RestaurantConfig config = GetCommands.getRestaurantConfig(guiController);
//		HashMap<LocalDate, String[]> date = config.getSpecialHours();
//		for(Map.Entry<LocalDate,String[]> entry:date.entrySet()) {
//			allReservations = GetCommands.getReservationsNotInTimeRange(entry.getKey(), LocalTime.parse(entry.getValue()[0]), LocalTime.parse(entry.getValue()[1]), guiController);
//			for(Reservation reservation : allReservations) {
//				cancelReservation(reservation,guiController);
//			}
//		}
//	}
	
	
	
	public static List<String> generateTimeSlots(String openTime, String closeTime) {
	    List<String> timeSlots = new ArrayList<>();
	    if (openTime == null || closeTime == null ) {
	        return timeSlots;
	    }
	    try {
	        LocalTime start = LocalTime.parse(openTime); 
	        LocalTime end = LocalTime.parse(closeTime);
	        LocalTime current = start;
	        while (!current.isAfter(end)) {
	            timeSlots.add(current.toString()); // toString() automatically outputs "HH:mm" if seconds are 00
	            current = current.plusMinutes(30);
	        }
	    } catch (Exception e) {
	        System.out.println("Error generating time slots: " + e.getMessage());
	    }
	    return timeSlots;
	}
	
	
	

}