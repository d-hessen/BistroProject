package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dataLayer.*;
import domainLogic.ServerFrameController;
import common.Status;

public class GetCommands {
	//======================================
	//GET OPERATIONS WITH RESERVATION
	//======================================
	//Retrieve a reservation by ID
    public static Reservation getReservation(Integer id, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM reservation WHERE reservation_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                	Reservation toReturn = new Reservation(
                        	rs.getInt("reservation_number"),
                        	new DateTime(rs.getString("reservation_date"),rs.getString("reservation_time")),
                        	rs.getInt("number_of_guests"),
                        	rs.getInt("member_id"),
                            new Guest(rs.getString("guest_full_name"), 
                            		rs.getString("guest_phone"), 
                            		rs.getString("email"))
                        );
                	Status status = Status.valueOf(rs.getString("status"));
                	toReturn.setStatus(status);
                	toReturn.setDateOfPlacingReservation(rs.getString("created_at"));
                	return toReturn;
                }
                else {
					guiController.addToConsole("Reservation not found for ID: " + id);
                }
            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error fetching reservation: " + e.getMessage());
        }
        return null;
    }
    //Method that return List of reserved time slots for @date
  	public List<DateTime> getReservedTimeSlots(String date, ServerFrameController guiController){
  		Connection conn = dbController.getInstance().getConnection();
  		List<DateTime> listOfTakenSlots = new ArrayList<>();
  		
  		String sql = "SELECT reservation_time from reservation WHERE reservation_date = ?";
  		
  		try (PreparedStatement ps = conn.prepareStatement(sql)){
  			ps.setString(0, date);
  			try (ResultSet rs = ps.executeQuery()) {
                  if (rs.next()) {
                      listOfTakenSlots.add(new DateTime(rs.getString("reservation_date"),rs.getString("reservation_time")));
                  }
                  return listOfTakenSlots;
              }
  		} catch(SQLException e) {
  			guiController.addToConsole("Error during creating list of taken slots: " +e.getMessage());
  			e.printStackTrace();
  		}
  		return null;
  	}
  	
  	//Method that gets reservation by member card code
    public Reservation findUpcomingReservationByCode(String cardCode, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Reservation foundReservation = null;
        int memberId = -1;
        String memberSql = "SELECT member_id FROM members WHERE card_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(memberSql)) {
            ps.setString(1, cardCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    memberId = rs.getInt("member_id");
                } else {
                    return null; // Member code not found
                }
            }
        } catch (Exception e) { 
        	guiController.addToConsole("Error checking member code: " + e); 
        	return null; 
        }

        //Find today's reservations, check so not checked in yet
        String resSql = "SELECT r.* FROM reservation r " +
                        "LEFT JOIN visits v ON r.reservation_number = v.reservation_number " +
                        "WHERE r.member_id = ? AND r.reservation_date = ? AND v.visit_id IS NULL";
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 

        try (PreparedStatement ps = conn.prepareStatement(resSql)) {
            ps.setInt(1, memberId);
            ps.setString(2, todayStr);
            
            try (ResultSet rs = ps.executeQuery()) {
                LocalTime now = LocalTime.now();
                LocalTime fifteenMinsBefore = now.minusMinutes(15);
                LocalTime fifteenMinsLater = now.plusMinutes(15);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); 

                while (rs.next()) {
                    String timeStr = rs.getString("reservation_time");
                    LocalTime resTime = LocalTime.parse(timeStr, timeFormatter);
                    // Check if reservation time is between NOW-15m and NOW+15m
                    // Allow checking in slightly early (e.g., 15 mins before). 
                    if ((resTime.isAfter(fifteenMinsBefore) || resTime.equals(now)) && resTime.isBefore(fifteenMinsLater)) {
                    	DateTime dt = new DateTime(rs.getString("reservation_date"), rs.getString("reservation_time"));
                    	Guest guestInfo = null;
                        foundReservation = new Reservation(
                        		 rs.getInt("reservation_number"),
                                 dt,
                                 rs.getInt("number_of_guests"),
                                 rs.getInt("member_id"),
                                 guestInfo
                        );
                        break; 
                    }
                }
            }
        } catch (Exception e) {
            guiController.addToConsole("Error finding reservation: " + e.getMessage());
        }
        return foundReservation;
    }

	// Retrieve reservations by phone number
	public static List<Reservation> getReservationsByPhoneNumber(String phoneNumber,ServerFrameController guiController) {
	    List<Reservation> reservations = new ArrayList<>();
	    Connection conn = dbController.getInstance().getConnection();
	    String sql = "SELECT * FROM reservation WHERE guest_phone = ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, phoneNumber);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Reservation reservation = new Reservation(rs.getInt("reservation_number"),
	                		new DateTime(
                            rs.getString("reservation_date"),
                            rs.getString("reservation_time")
	                        ),
	                        rs.getInt("number_of_guests"),
	                        rs.getInt("member_id"),
	                        new Guest(
	                                rs.getString("guest_full_name"),
	                                rs.getString("guest_phone"),
	                                rs.getString("email"))
	                );
	                Status status = Status.valueOf(rs.getString("status"));
	                reservation.setStatus(status);
	                reservation.setDateOfPlacingReservation(
	                        rs.getString("created_at")
	                );
	                reservations.add(reservation);
	            }
	        }

	    } catch (SQLException e) {
	        guiController.addToConsole(
	                "Error fetching reservations by phone: " + e.getMessage()
	        );
	    }

	    if (reservations.isEmpty()) {
	        guiController.addToConsole(
	                "No reservations found for phone number: " + phoneNumber
	        );
	    }

	    return reservations;
	}

	//======================================
	//GET GUEST
	//======================================
	//Get guest by phone number/email from reservation
	public Guest getGuest(Integer phone, String email, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		//Get guest by phone
		if(phone != null) {
		    String sql = "SELECT guest_full_name,email FROM reservation WHERE guest_phone = ?";
	        
		    try (PreparedStatement ps = conn.prepareStatement(sql)) {
		    	ps.setInt(1, phone);
		        try (ResultSet rs = ps.executeQuery()) {
		        	if (rs.next()) {
		        		return new Guest(
		        				rs.getString("guest_full_name"),
		                        rs.getString("guest_phone"),
		                        rs.getString("email")
		                    );
		                }
		            }
		       	} catch (SQLException e) {
		       		guiController.addToConsole("Error fetching guest by phone number: " + e.getMessage());
		        }
		}
		else { //Get guest by email
			String sql = "SELECT guest_full_name,guest_phone FROM reservation WHERE email = ?";
	        
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
			    ps.setString(1, email);
			    try (ResultSet rs = ps.executeQuery()) {
			        if (rs.next()) {
			        	return new Guest(
			        			rs.getString("guest_full_name"),
			                    rs.getString("guest_phone"),
			                    rs.getString("email")
			                );
			               }
			           }
			     } catch (SQLException e) {
			    	 guiController.addToConsole("Error fetching guest by email: " + e.getMessage());
			      }
		}
		return null;
	}
	
	//Get guest by waitingId from waiting_list
	public static Guest getGuest(Integer waitingId, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		//Get guest by phone
	    String sql = "SELECT * FROM waiting_list WHERE waiting_id = ?";
        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, waitingId);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		return new Guest(
	        				rs.getString("guest_full_name"),
	                        rs.getString("guest_phone"),
	                        rs.getString("email")
	                    );
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching guest by waiting id: " + e.getMessage());
	        }
		return null;
	}
	
	
	
	//======================================
	//GET MEMBER
	//======================================
	//Get member by phone number
	public static Member getMember(Integer phone, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
	    String sql = "SELECT * FROM members WHERE phone = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, phone);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		Member toReturn = new Member(
	        				rs.getString("full_name"),
	                        rs.getString("phone"),
	                        rs.getString("email"),
	                        rs.getString("password")
	                    );
	        		toReturn.setMemberId(rs.getInt("member_id"));
	        		toReturn.setCardCode(rs.getString("card_code"));
	        		return toReturn;
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return null;
	}
	//Get member by email
	public static Member getMember(String email, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
	    String sql = "SELECT * FROM members WHERE email = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, email);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		Member toReturn = new Member(
	        				rs.getString("full_name"),
	                        rs.getString("phone"),
	                        rs.getString("email"),
	                        rs.getString("password")
	                    );
	        		toReturn.setMemberId(rs.getInt("member_id"));
	        		toReturn.setCardCode(rs.getString("card_code"));
	        		return toReturn;
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return null;
	}
	//Get member by card code
	public Member getMemberByCode(String code) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM members WHERE card_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) return new Member(rs.getString("full_name"), rs.getString("phone"), rs.getString("email"), null);
            }
        } catch (Exception e) {}
        return null;
    }
	
	//======================================
	//GET TABLE
	//======================================
	//Get table
	public static Table getTable(Integer tableId, ServerFrameController guiController) {
			Connection conn = dbController.getInstance().getConnection();
		        
		    String sql = "SELECT * FROM tables WHERE table_number = ?";
		        
		    try (PreparedStatement ps = conn.prepareStatement(sql)) {
		    	ps.setInt(1, tableId);
		        try (ResultSet rs = ps.executeQuery()) {
		        	if (rs.next()) {
		        		return new Table(
		        				rs.getInt("table_number"),
		                        rs.getInt("capacity"),
		                        rs.getBoolean("is_active")
		                    );
		                }
		            }
		       	} catch (SQLException e) {
		       		guiController.addToConsole("Error fetching table: " + e.getMessage());
		        }
		        return null;
	}
	//Method to find available table for @partySize
	public Integer getAvailableTableId(int partySize) {
        Connection conn = dbController.getInstance().getConnection();
        // Find a table with enough capacity that is active (exists) and NOT occupied (no active visit)
        String sql = "SELECT t.table_id FROM tables t " +
                     "LEFT JOIN visits v ON t.table_id = v.table_id AND v.is_active = 1 " +
                     "WHERE t.is_active = 1 AND t.capacity >= ? AND v.visit_id IS NULL " +
                     "ORDER BY t.capacity ASC LIMIT 1"; 
                     
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partySize);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("table_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No table found
    }
	
	//Fetch all tables and check if they are currently occupied
	public ArrayList<Table> getAllTablesWithStatus(ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        ArrayList<Table> tables = new ArrayList<>();
        
        // This query checks if there is an ACTIVE visit (is_active=1) on the table
        String sql = "SELECT t.table_number, t.capacity, t.is_active, v.visit_id " +
                     "FROM tables t " +
                     "LEFT JOIN visits v ON t.table_id = v.table_id AND v.is_active = 1 " +
                     "ORDER BY t.table_number ASC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Table t = new Table(
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getBoolean("is_active")
                );
                // If visit_id is not null, the table is occupied
                t.setOccupied(rs.getObject("visit_id") != null);
                if(t.isOccupied()) {
                	Visit currentVisit = getVisit((Integer)rs.getObject("visit_id"), guiController);
                	t.setCurrentVisit(currentVisit);
                }
                tables.add(t);
            }
            return tables;
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching tables: " + e.getMessage());
        }
        return null;
    }
	
	public static Reservation getReservationVerificationCode(String codeStr, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		
		int code;
		try {
			code = Integer.parseInt(codeStr);
		} catch (NumberFormatException e) {
			guiController.addToConsole("Invalid verification code format: " + codeStr);
			return null;
		}

		String sql = "SELECT * FROM reservation WHERE verification_code = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, code); 
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Reservation toReturn = new Reservation(
						rs.getInt("reservation_number"),
						new DateTime(rs.getString("reservation_date"), rs.getString("reservation_time")), 
						rs.getInt("number_of_guests"),
						rs.getInt("member_id"), 
						new Guest(
								rs.getString("guest_full_name"), 
								rs.getString("guest_phone"), 
								rs.getString("email")
						)
					);					
					toReturn.setVerificationCode(rs.getString("verification_code"));
					
					if (rs.getString("status") != null) {
						toReturn.setStatus(Status.valueOf(rs.getString("status")));
					}
					
					toReturn.setDateOfPlacingReservation(rs.getString("created_at"));
					
					return toReturn;
				} else {
					guiController.addToConsole("Reservation not found for verification code: " + code);
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservation by code: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	//======================================
	//GET STAFF
	//======================================
	//Get staff by username
	public static Staff getStaff(String username, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM staff WHERE username = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, username);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		boolean isManager = false;
	        		if(rs.getString("role").equals("manager")) {
	        			isManager = true;
	        		}
	        		Staff toReturn = new Staff(
	        				rs.getString("username"),
	                        rs.getString("password"),
	                        rs.getString("full_name"),
	                        isManager
	                    );
	        		toReturn.setStaffId(rs.getInt("staff_id"));
	        		return toReturn;
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching staff: " + e.getMessage());
	        }
	        return null;
	}
	
	
	
	//======================================
	//GET VISIT
	//======================================
	//Get visit by visitId 
	public static Visit getVisit(Integer visitId, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM visits WHERE visit_id = ?";
	    Visit toReturn = null;
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, visitId);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		toReturn = constructVisitFromResultSet(rs, conn, guiController);
	        		Table assignedTable = getTable(rs.getInt("table_id"), guiController);
	        		if(rs.getObject("reservation_number") != null) { //If visit were reserved
	        			Reservation assignedReservation = getReservation((Integer)rs.getObject("reservation_number"),guiController);
	        			toReturn = new Visit(assignedReservation, assignedTable);
	        			toReturn.setPartySize(assignedReservation.getNumberOfGuests());
	        		}else {
	        			Guest assignedGuest = getGuest(rs.getInt("waiting_id"), guiController);
	        			toReturn = new Visit(assignedGuest, assignedTable);
	        			toReturn.setPartySize(rs.getInt("party_size"));
	        		}
	        		if(toReturn != null) {
	        			//Timestamps of start and end visit
	        			LocalDateTime localStartDateTime = rs.getTimestamp("start_time").toLocalDateTime();
	        			if(rs.getObject("end_time") != null) {
	        				LocalDateTime localEndDateTime = rs.getTimestamp("end_time").toLocalDateTime();
	        				toReturn.setEndTime(new DateTime(localEndDateTime.toLocalDate().toString(), localEndDateTime.toLocalTime().toString()));
	        			}
	        			toReturn.setActive(rs.getBoolean("is_active"));
	        			toReturn.setStartTime(new DateTime(localStartDateTime.toLocalDate().toString(), localStartDateTime.toLocalTime().toString()));
	        			toReturn.setVisitId(visitId);
	        			toReturn.setBillOfVisit(getBill(visitId, guiController));
	        		}
	            }
	        }
	      } catch (SQLException e) {
	       		guiController.addToConsole("Error fetching staff: " + e.getMessage());
	      }
	      return toReturn;
	}
	
	//Get all visits for specific member
	public static ArrayList<Visit> getMemberVisits(Integer memberId, ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    ArrayList<Visit> visits = new ArrayList<>();
	    
	    // Join visits with reservation to filter by member_id
	    String sql = "SELECT v.* FROM visits v " +
	                 "JOIN reservation r ON v.reservation_id = r.reservation_number " +
	                 "WHERE r.member_id = ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, memberId);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                visits.add(constructVisitFromResultSet(rs, conn, guiController));
	            }
	        }
	    } catch (SQLException e) {
	        guiController.addToConsole("Error fetching member visits: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return visits;
	}
	
	//Get current dinning sessions in restaurant
	public static ArrayList<Visit> getActiveVisits(ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    ArrayList<Visit> visits = new ArrayList<>();

	    String sql = "SELECT * FROM visits WHERE is_active = TRUE";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                visits.add(constructVisitFromResultSet(rs, conn, guiController));
	            }
	        }
	    } catch (SQLException e) {
	        guiController.addToConsole("Error fetching active visits: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return visits;
	}
	
	//Helper method to construct visit
	private static Visit constructVisitFromResultSet(ResultSet rs, Connection conn, ServerFrameController guiController) throws SQLException {
		Visit visit = null;
	    int visitId = rs.getInt("visit_id");

	    Table assignedTable = getTable(rs.getInt("table_id"), guiController);

		if(rs.getObject("reservation_number") != null) { //If visit were reserved
			Reservation assignedReservation = getReservation((Integer)rs.getObject("reservation_number"),guiController);
			visit = new Visit(assignedReservation, assignedTable);
			visit.setPartySize(assignedReservation.getNumberOfGuests());
		}else {
			Guest assignedGuest = getGuest(rs.getInt("waiting_id"), guiController);
			visit = new Visit(assignedGuest, assignedTable);
			visit.setPartySize(rs.getInt("party_size"));
		}

		if(visit != null) {
			//Timestamps of start and end visit
			LocalDateTime localStartDateTime = rs.getTimestamp("start_time").toLocalDateTime();
			if(rs.getObject("end_time") != null) {
				LocalDateTime localEndDateTime = rs.getTimestamp("end_time").toLocalDateTime();
				visit.setEndTime(new DateTime(localEndDateTime.toLocalDate().toString(), localEndDateTime.toLocalTime().toString()));
			}
			visit.setActive(rs.getBoolean("is_active"));
			visit.setStartTime(new DateTime(localStartDateTime.toLocalDate().toString(), localStartDateTime.toLocalTime().toString()));
			visit.setVisitId(visitId);
			visit.setBillOfVisit(getBill(visitId, guiController));
		}

	    return visit;
	}
	
	
	//======================================
	//GET BILL
	//======================================
	//Get bill by visitId 
	public static Bill getBill(Integer visitId, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM bills WHERE visit_id = ?";
	    Bill toReturn = null;
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, visitId);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		toReturn = new Bill();
	        		toReturn.setTotalAmount(rs.getDouble("total_amount"));
	        		toReturn.setDiscountAmount(rs.getDouble("discount_amount"));
	        		toReturn.setFinalAmount(rs.getDouble("final_amount"));
	        		toReturn.setPaid(rs.getBoolean("is_paid"));
	        		if(rs.getObject("payment_time") != null) {
		        		LocalDateTime localPaymentDateTime = rs.getTimestamp("payment_time").toLocalDateTime();
		        		toReturn.setPaymentTime(new DateTime(localPaymentDateTime.toLocalDate().toString(), localPaymentDateTime.toLocalTime().toString()));
	        		}
	        		
	            }
	        }
	    } catch (SQLException e) {
	       guiController.addToConsole("Error fetching bill: " + e.getMessage());
	    }
	    return toReturn;
	}
}
