package domainLogic;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import common.Action;
import common.BistroMessage;
import common.Status;
import dataLayer.Reservation;
import dataLayer.Table;
import dataLayer.Visit;
import databaseController.CreateCommands;
import databaseController.GetCommands;
import databaseController.UpdateCommands;
import utils.EmailSend;

/**
 * Controller responsible for managing active Visits (both seated and waiting).
 * Handles logic for walk-ins, checking in reservations, billing, and waiting list management.
 */
public class VisitController {
    
    /**
     * Creates a visit for a walk-in customer. 
     * If a table is available immediately, it creates a seated visit.
     * Otherwise, it adds the customer to the waiting list.
     * * @param toCreate      the Visit object to create
     * @param guiController reference to the server GUI controller for logging
     * @return a BistroMessage containing the Visit or a status message
     */
    public static BistroMessage createWalkInVisit(Visit toCreate, ServerFrameController guiController) {
    	Action action = Action.VISIT_NOW;
    	if(toCreate.getVerificationCode() == null) {
        	toCreate.setVerificationCode(ReservationController.generateVerificationCode());
    	}else {
    		action = Action.CHECK_IN_CUSTOMER; 
    	}
    	Visit foundCreated = GetCommands.getVisitByVerificationCode(toCreate.getVerificationCode(), guiController);
    	if(foundCreated != null) {
    		return new BistroMessage(action, foundCreated);
    	}
    	
        Integer foundTableId = findTableForVisit(toCreate, guiController);
        if(toCreate.getTable() != null && toCreate.getTable().getTableNumber() != null) {
        	foundTableId = toCreate.getTable().getTableNumber();
        }
        if (foundTableId != null) {
            //table is available immediately and doesn't conflict with future reservations
            Visit created = CreateCommands.createSeatedWalkInVisit(toCreate, foundTableId, guiController);
            if(created != null) {
            	UpdateCommands.updateVisitInWaitingList(toCreate,guiController);
                return new BistroMessage(action, created);
            } else {
                return new BistroMessage(action, "Error: DB Failed to create seated visit.");
            }
        } else {// No table available, add to Waiting List if not there yet
        	if(toCreate.getWaitingId() == null) {
                Visit waiting = CreateCommands.createWaitingWalkInVisit(toCreate, guiController);
                if(waiting != null) {
                    return new BistroMessage(action, waiting);
                } else {
                    return new BistroMessage(action, "Error: DB Failed to create waiting visit.");
                }
        	} else {
        		return new BistroMessage(action, "Wait: No free table at the moment");
        	}
            
        }
    }

    /**
     * Finds a table available NOW that does not conflict with reservations in the upcoming 2 hours.
     * * @param allTables    list of all tables
     * @param reservations list of upcoming reservations
     * @param partySize    number of guests
     * @return the table number if found, or null otherwise
     */
    private static Integer findTable(List<Table> allTables, List<Reservation> reservations, Integer partySize) {
        
        //Filter tables that are currently FREE and fit the party size
        List<Table> candidateTables = allTables.stream()
                .filter(table -> table.isActive() && !table.isOccupied() && table.getTableCapacity() >= partySize)
                .sorted(Comparator.comparingInt(Table::getTableCapacity)) // Check smallest fit first
                .collect(Collectors.toList());

        if (candidateTables.isEmpty()) {
            return null; // No tables physically free right now
        }

        //Conflict Check
        //For each candidate table, we check: 
        //If we give this table to the Walk-In (blocking it for 2 hours), can we still arrange ALL upcoming reservations?
        
        for (Table candidate : candidateTables) {
            if (canAccommodateReservationsIfTableTaken(allTables, reservations, candidate)) {
                return candidate.getTableNumber(); // Found a valid table!
            }
        }

        return null; // No table found that doesn't cause a conflict
    }

    /**
     * Simulation logic: Checks if all reservations can still be seated if a specific table is taken by a walk-in.
     * Uses a greedy matching strategy on a simulated inventory of tables.
     * * @param allTables          all tables
     * @param reservations       reservations to fit
     * @param tableTakenByWalkIn the candidate table to remove from inventory
     * @return true if all reservations can fit, false otherwise
     */
    private static boolean canAccommodateReservationsIfTableTaken(List<Table> allTables, List<Reservation> reservations, Table tableTakenByWalkIn) {
        if (reservations.isEmpty()) return true;

        //Create a "Simulation" inventory of tables available for the reservations.
        //We assume occupied tables (other than our candidate) will NOT be free in the next 2 hours
        
        //SimulationInventory = All Tables(avaialable) - tableTakenByWalkIn (Candidate table)
        List<Table> availableInventory = allTables.stream()
                .filter(t -> t.isActive() && !t.isOccupied() && t.getTableNumber() != tableTakenByWalkIn.getTableNumber())
                .collect(Collectors.toList());

        //Try to fit all reservations into this inventory
        //Sort reservations
        //We don't assign specific tables to reservations in the DB, we just need to know if an assignment EXISTS.
        List<Reservation> sortedReservations = new ArrayList<>(reservations);
        sortedReservations.sort(Comparator.comparingInt(Reservation::getNumberOfGuests).reversed());
        //Greedy Match:
        //For every reservation, find the smallest available table in inventory that fits it.
        //If found, remove table from inventory and continue.
        //If not found, then taking 'tableTakenByWalkIn' caused a conflict => There's no arrangement

        List<Table> simulationInventory = new ArrayList<>(availableInventory);
        
        for (Reservation reservation : sortedReservations) {
            Table bestFit = null;
            int bestFitIndex = -1;

            // Find best fit in simulation inventory
            for (int tableIndex = 0; tableIndex < simulationInventory.size(); tableIndex++) {
                Table table = simulationInventory.get(tableIndex);
                if (table.getTableCapacity() >= reservation.getNumberOfGuests()) {
                    if (bestFit == null || table.getTableCapacity() < bestFit.getTableCapacity()) {
                        bestFit = table;
                        bestFitIndex = tableIndex;
                    }
                }
            }

            if (bestFit != null) {
                // We found a place for this reservation
            	//Remove table from available tables
                simulationInventory.remove(bestFitIndex);
            } else {
                // We could not find a place for this reservation if we accept the walk-in.
                return false; 
            }
        }

        return true; // All reservations fit!
    }
    
    /**
     * Helper to find a suitable table for a visit considering current status and upcoming reservations.
     * * @param visit         the visit object
     * @param guiController server controller
     * @return table ID or null
     */
    private static Integer findTableForVisit(Visit visit, ServerFrameController guiController) {
        //Get all tables with their current occupied status (active visits)
        ArrayList<Table> allTables = GetCommands.getAllTablesWithStatus(guiController);
        
        //Get reservations for the next 2 hours
        LocalTime now = LocalTime.now();
        LocalTime twoHoursLater = now.plusHours(2);
        List<Reservation> upcomingReservations = GetCommands.getUpcomingReservationsInTimeRange(now, twoHoursLater, guiController);
        //If visit was reserved - remove occurence of reservation from upcoming reservations
        //So it doesnt show up twice
        if(visit.getReservation() != null) {
        	for (Reservation reservation : upcomingReservations) {
				if(reservation.getReservationId() == visit.getReservation().getReservationId()) {
					upcomingReservations.remove(reservation);
				}
			}
        }
        //find candidate table
        Integer foundTableId = findTable(allTables, upcomingReservations, visit.getPartySize());
        return foundTableId;
    }
    
    /**
     * Creates a seated visit from an existing Reservation.
     * * @param reservation   the existing reservation
     * @param guiController server controller
     * @return a BistroMessage with the created Visit or a status message
     */
    public static BistroMessage createReservatedVisit(Reservation reservation, ServerFrameController guiController) {
    	Visit foundCreated = GetCommands.getVisitByVerificationCode(reservation.getVerificationCode(), guiController);
    	if(foundCreated != null) {
    		return new BistroMessage(Action.CHECK_IN_CUSTOMER, foundCreated);
    	}
    	Visit toCreate = new Visit(reservation,null);
        Integer foundTableId = findTableForVisit(toCreate, guiController);
        if (foundTableId != null) {
            //table is available immediately and doesn't conflict with future reservations
            Visit created = CreateCommands.createVisit(reservation.getReservationId(), foundTableId, guiController);
            if(created != null) {
            	reservation.setStatus(Status.seated);
            	UpdateCommands.updateReservation(reservation, guiController);
                return new BistroMessage(Action.CHECK_IN_CUSTOMER, created);
            } else {
                return new BistroMessage(Action.CHECK_IN_CUSTOMER, "Error: DB Failed to create reservated visit.");
            }
        } else {
            // No table available, preparing table
            return new BistroMessage(Action.CHECK_IN_CUSTOMER, "Wait: Preparing your table");
        }
    }

    /**
     * Updates general details of a visit.
     * * @param toUpdate      BistroMessage containing the visit to update
     * @param guiController server controller
     * @return BistroMessage indicating success
     */
	public static BistroMessage updateVisit(BistroMessage toUpdate, ServerFrameController guiController) {
		//Return true if successfully changed
		//Return false if was an error
		return new BistroMessage(Action.START_VISIT, UpdateCommands.updateVisit(toUpdate, guiController));
	}
        
	/**
	 * Updates the bill amount for a visit. 
	 * If the bill is paid, it notifies the user and checks the waiting list.
	 * * @param toUpdate      BistroMessage containing the visit
	 * @param guiController server controller
	 * @return BistroMessage with the result
	 */
	public static BistroMessage updateBillOfVisit(BistroMessage toUpdate, ServerFrameController guiController) {
		BistroMessage updateResult = new BistroMessage(toUpdate.getAction(), UpdateCommands.updateBillForVisit((Visit)toUpdate.getData(), guiController));
		if(toUpdate.getAction() == Action.BILL_PAID && updateResult.getData() != null) {
			UpdateCommands.updateVisit(toUpdate, guiController);
			EmailSend.sendBillNotification((Visit)toUpdate.getData());
			checkWaitingListAndNotify(guiController);
		}
		return updateResult;
	}
	
	/**
	 * Checks if any waiting list groups can be seated now that a table has freed up.
	 * If a match is found, notifies the user and reserves the table.
	 * * @param guiController server controller
	 */
    public static void checkWaitingListAndNotify(ServerFrameController guiController) {
        ArrayList<Table> allTables = GetCommands.getAllTablesWithStatus(guiController);
        
        LocalTime now = LocalTime.now();
        LocalTime twoHoursLater = now.plusHours(2);
        List<Reservation> upcomingReservations = GetCommands.getUpcomingReservationsInTimeRange(now, twoHoursLater, guiController);

        List<Visit> waitingList = GetCommands.getWaitingList(guiController);

        for (Visit waitingVisit : waitingList) {
        	if(!waitingVisit.getStatus().name().equals("waiting")) {
        		return;
        	}

            Integer foundTableNum = findTable(allTables, upcomingReservations, waitingVisit.getPartySize());

            if (foundTableNum != null) {
                EmailSend.sendTableReadyNotification(waitingVisit);
                UpdateCommands.updateWaitingListStatus(waitingVisit.getWaitingId(), "notified", foundTableNum, guiController);
                
                guiController.addToConsole("Table " + foundTableNum + " is now held for Waiting Group " + waitingVisit.getWaitingId());

                for (Table t : allTables) {
                    if (t.getTableNumber() == foundTableNum) {
                        t.setOccupied(true); 
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Background task to process waiting list entries that have expired.
     * * @param guiController server controller
     */
    public static void processWaitingListExpirations(ServerFrameController guiController) {
        List<Visit> expiredList = GetCommands.getExpiredWaitingListEntries(guiController);
        if (expiredList.isEmpty()) {
        	return;
        }
        boolean tableFreed = false;
        for (Visit visit : expiredList) {
            EmailSend.sendWaitingListCancellation(visit);
            boolean success = UpdateCommands.cancelWaitingListEntry(visit.getWaitingId(), guiController);   
            if (success) {
                guiController.addToConsole("Auto-cancelled Waiting Group " + visit.getWaitingId() + " (Time limit exceeded).");
                tableFreed = true;
            }
        }
        if (tableFreed) {
            checkWaitingListAndNotify(guiController);
            tableFreed = false;
        }
    }
    
    /**
     * Background task to process auto-billing for visits.
     * * @param guiController server controller
     */
    public static void processAutoBilling(ServerFrameController guiController) {
        List<Visit> dueVisits = GetCommands.getVisitsDueForBilling(guiController);
        
        for (Visit visit : dueVisits) {
            EmailSend.sendBillNotification(visit);
            
            UpdateCommands.markBillSent(visit.getVisitId(), guiController);
            
            guiController.addToConsole("Sent auto-bill to Visit ID: " + visit.getVisitId());
        }
    }
    
//    // Ensure this is called when a visit ends
//    public static BistroMessage endVisit(Visit visit, ServerFrameController guiController) {
//        boolean success = UpdateCommands.endVisit(visit, guiController);
//        if (success) {
//            // Trigger the check immediately after a table frees up
//            checkWaitingListAndNotify(guiController);
//            return new BistroMessage(Action.END_VISIT, true);
//        }
//        return new BistroMessage(Action.END_VISIT, false);
//    }
}