package databaseController;

import java.sql.Connection;
import java.sql.Date;
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
/**
 * Handles all sql select commands
 */
public class GetCommands {
	//======================================
	//GET OPERATIONS WITH RESERVATION
	//======================================

	/**
	 * Get reservation by it's reservation id
	 * @param id reservation id
	 * @param guiController logging controller
	 * @return Reservation object on success, null on fialure
	 */
    public static Reservation getReservation(Integer id, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		String sql = "SELECT * FROM reservation WHERE reservation_number = ?";
		
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Reservation res = mapRowToReservation(rs, guiController);
					//If member reserved
					if (res.getMemberId() != null && res.getMemberId() > 0) {
						Member fullMember = getMemberById(res.getMemberId(), guiController);
						if (fullMember != null) {
							res.setGuest(fullMember);
						}
					}
					return res;
				} 
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservation: " + e.getMessage());
		}
		return null;
	}

    /**
     * Find today's reservation in +/- 15 mins from now by member card code
     * @param cardCode member card code
     * @param guiController logging controller
     * @return reservation object on success, null otherwise
     */
    public static Reservation findUpcomingReservationByCode(String cardCode, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		int memberId = -1;
		//Get memberId from members
		String memberSql = "SELECT member_id FROM members WHERE card_code = ?";
		try (PreparedStatement ps = conn.prepareStatement(memberSql)) {
			ps.setString(1, cardCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					memberId = rs.getInt("member_id");
				} else {
					return null; //Member code not found
				}
			}
		} catch (Exception e) { 
			guiController.addToConsole("Error checking member code: " + e); 
			return null; 
		}
		//Find reservation
		String resSql = "SELECT r.* FROM reservation r " +
						"LEFT JOIN visits v ON r.reservation_number = v.reservation_number " +
						"WHERE r.member_id = ? AND r.reservation_date = ? AND v.visit_id IS NULL AND status = 'approved'";
		
		LocalDate today = LocalDate.now();
		String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 

		try (PreparedStatement ps = conn.prepareStatement(resSql)) {
			ps.setInt(1, memberId);
			ps.setString(2, todayStr);
			
			try (ResultSet rs = ps.executeQuery()) {
				LocalTime now = LocalTime.now();
				LocalTime windowStart = now.minusMinutes(15);
				LocalTime windowEnd = now.plusMinutes(15);
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); 

				while (rs.next()) {
					String timeStr = rs.getString("reservation_time");
					LocalTime resTime = LocalTime.parse(timeStr, timeFormatter);
					
					// Check time window (NOW +/- 15 mins)
					if ((resTime.isAfter(windowStart) || resTime.equals(now)) && resTime.isBefore(windowEnd)) {
						return mapRowToReservation(rs, guiController); // Found valid reservation
					}
				}
			}
		} catch (Exception e) {
			guiController.addToConsole("Error finding reservation by member card: " + e.getMessage());
		}
		return null;
	}
    
    /**
     * Get all reservations between @param start and @param end for today
     * @param start start time of wanted window
     * @param end end time of wanted window
     * @param guiController logging controller
     * @return List of Reservation objects on success, otherwise null
     */
    public static List<Reservation> getUpcomingReservationsInTimeRange(LocalTime start, LocalTime end, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		List<Reservation> upcoming = new ArrayList<>();
		String today = LocalDate.now().toString();

		String sql = "SELECT * FROM reservation WHERE reservation_date = ? " +
					 "AND reservation_time >= ? AND reservation_time <= ? AND status != 'CANCELLED'";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, today);
			ps.setString(2, start.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
			ps.setString(3, end.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					upcoming.add(mapRowToReservation(rs, guiController));
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching upcoming reservations: " + e.getMessage());
		}
		return upcoming;
	}
    
    public static List<Reservation> getReservationsNotInTimeRange(LocalDate date, LocalTime start, LocalTime end, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        List<Reservation> outsideRange = new ArrayList<>();

        String sql = "SELECT * FROM reservation WHERE reservation_date = ? " +
                     "AND (reservation_time < ? OR reservation_time > ?) " + 
                     "AND status != 'CANCELLED'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            ps.setString(2, start.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            ps.setString(3, end.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    outsideRange.add(mapRowToReservation(rs, guiController));
                }
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching reservations outside range: " + e.getMessage());
        }
        return outsideRange;
    }	
    /**
     * Get reservations by phone number
     * @param phoneNumber guest phone number
     * @param guiController logging controller
     * @return List of reservation objects on success, null otherwise
     */
	public static List<Reservation> getReservationsByPhoneNumber(String phoneNumber, ServerFrameController guiController) {
		List<Reservation> reservations = new ArrayList<>();
		Connection conn = dbController.getInstance().getConnection();
		String sql = "SELECT * FROM reservation WHERE guest_phone = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, phoneNumber);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					reservations.add(mapRowToReservation(rs, guiController));
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservations by phone: " + e.getMessage());
		}
		return reservations;
	}
	
	/**
	 * Reservations that approved but passed 15 mins time to check-in
	 * @param guiController logging controller
	 * @return List of Reservation objects on success, null otherwise
	 */
	public static List<Reservation> getExpiredReservations(ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		List<Reservation> expiredList = new ArrayList<>();
		
		String sql = "SELECT * FROM reservation " +
					 "WHERE status = 'approved' " +
					 "AND TIMESTAMP(CONCAT(reservation_date, ' ', reservation_time)) < NOW() - INTERVAL 15 MINUTE";

		try (PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				expiredList.add(mapRowToReservation(rs, guiController));
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching expired reservations: " + e.getMessage());
		}
		return expiredList;
	}

	/**
	 * Get reservations that have start_time in less than 2 hours from NOW
	 * @param guiController logging controller
	 * @return List of Reservation objects on success, null otherwise
	 */
	public static List<Reservation> getReservationsForReminder(ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		List<Reservation> reminders = new ArrayList<>();
		
		String sql = "SELECT * FROM reservation " +
					 "WHERE status = 'approved' " +
					 "AND reminder_sent = FALSE " +
					 "AND TIMESTAMP(CONCAT(reservation_date, ' ', reservation_time)) <= DATE_ADD(NOW(), INTERVAL 2 HOUR) " +
					 "AND TIMESTAMP(CONCAT(reservation_date, ' ', reservation_time)) > NOW()";

		try (PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				reminders.add(mapRowToReservation(rs, guiController));
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservations for reminder: " + e.getMessage());
		}
		return reminders;
	}
	
	/**
	 * Serach reservation by phone or email
	 * @param contactInput email or phone number
	 * @param guiController logging controller
	 * @return List of reservations on success, null on failure
	 */
    public static List<Reservation> getReservationsByContactInfo(String contactInput, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        List<Reservation> list = new ArrayList<>();

        String sql = "SELECT * FROM reservation WHERE guest_phone = ? OR email = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactInput);
            ps.setString(2, contactInput);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReservation(rs, guiController));
                }
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching by contact info: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * Finds overlapping reservations for given time window. To check table availability
     * @param windowStart start of window time 
     * @param windowEnd end of window time
     * @param guiController logging controller
     * @return List of reservation objects or null on failure
     */
	public static List<Reservation> getReservationOverlap(LocalDateTime windowStart, LocalDateTime windowEnd, ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    List<Reservation> list = new ArrayList<>();
	    //Reservation overlaps if it starts before window time ends AND ends after window starts
	    String sql = "SELECT * FROM reservation "
                + "WHERE status NOT IN ('cancelled', 'no_show') "
                + "AND CONCAT(reservation_date, ' ', reservation_time) < ? "
                + "AND DATE_ADD(CONCAT(reservation_date, ' ', reservation_time), INTERVAL 2 HOUR) > ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        ps.setString(1, windowEnd.format(formatter)); 
	        ps.setString(2, windowStart.format(formatter));
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	            	list.add(mapRowToReservation(rs, guiController));
	            }
	        }
	    } catch (SQLException e) {
	        guiController.addToConsole("Error fetching overlapping reservations: " + e.getMessage());
	    }
	    return list;
	}
	
	/**
	 * Get reservation using verification code
	 * @param codeStr verification code for specific reservation
	 * @param guiController logging controller
	 * @return Reservation object on success, on failure null
	 */
	public static Reservation getReservationVerificationCode(String codeStr, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();

		String sql = "SELECT * FROM reservation WHERE verification_code = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codeStr); 
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapRowToReservation(rs, guiController);
				} else {
					guiController.addToConsole("Reservation not found for verification code: " + codeStr);
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservation by code: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
    /**
     * Helper to map ResultSet row to reservation object
     * @param rs Result Set recieved from executing query
     * @return Reservation object on success, on failure null
     * @throws SQLException
     */
	private static Reservation mapRowToReservation(ResultSet rs, ServerFrameController guiController) throws SQLException {
		Integer memberId = (Integer) rs.getObject("member_id");
		if (rs.wasNull()) memberId = null;

		Guest guest = new Guest(
			rs.getString("guest_full_name"), 
			rs.getString("guest_phone"), 
			rs.getString("email")
		);

		Reservation res = new Reservation(
			rs.getInt("reservation_number"),
			new DateTime(rs.getString("reservation_date"), rs.getString("reservation_time")),
			rs.getString("verification_code"),
			rs.getInt("number_of_guests"),
			memberId,
			guest
		);
		
		if (res.getMemberId() != null && res.getMemberId() > 0) {
			Member fullMember = getMemberById(res.getMemberId(), guiController);
			if (fullMember != null) {
				res.setGuest(fullMember);
			}
		}

		try {
			res.setStatus(Status.valueOf(rs.getString("status")));
		} catch (Exception e) {
			res.setStatus(Status.pending);
		}
		
		res.setDateOfPlacingReservation(rs.getString("created_at"));
		return res;
	}
	//======================================
	//GET GUEST
	//======================================
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
	
	/**
	 * Get member by phone number
	 * @param phone member phone number
	 * @param guiController logging controller
	 * @return member object on success, null on failure
	 */
	public static Member getMember(Integer phone, ServerFrameController guiController) {
        return getMemberByField("phone", phone, guiController);
    }
	
	/**
	 * Get member by email
	 * @param email member email
	 * @param guiController logging controller
	 * @return member object on success, null on failure
	 */
	public static Member getMember(String email, ServerFrameController guiController) {
        return getMemberByField("email", email, guiController);
    }
	
	/**
	 * Get member by member card_code
	 * @param code member card_code
	 * @param guiController logging controller
	 * @return Member object on success, null on failure
	 */
	public static Member getMemberByCode(String code, ServerFrameController guiController) {
        return getMemberByField("card_code", code, guiController);
    }
	
	/**
	 * Get member by memberId
	 * @param memberId member id
	 * @param guiController logging controller
	 * @return member object on success
	 */
	public static Member getMemberById(Integer memberId, ServerFrameController guiController) {
        return getMemberByField("member_id", memberId, guiController);
    }
	
	/**
	 * Get member by specific field in db
	 * @param fieldName field name in dm
	 * @param value object to be searched (key)
	 * @param guiController logging controller
	 * @return member object on success, null on failure
	 */
	private static Member getMemberByField(String fieldName, Object value, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM members WHERE " + fieldName + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return constructMemberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching member by " + fieldName + ": " + e.getMessage());
        }
        return null;
    }
	
	/**
	 * Helper method to construct member from resultset recieved
	 * @param rs ResultSet from executing query
	 * @return on success Member object, on failure null
	 * @throws SQLException
	 */
	private static Member constructMemberFromResultSet(ResultSet rs) throws SQLException {
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
	//======================================
	//GET TABLE
	//======================================

	/**
	 * Get table by id
	 * @param tableId table id in sql db
	 * @param guiController logging controller
	 * @return Table object on success, null on failure
	 */
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
	
	/**
	 * Get all tables with active visits connected to them
	 * @param guiController logging controller
	 * @return ArrayList of Table objects on success, on failure null
	 */
	public static ArrayList<Table> getAllTablesWithStatus(ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        ArrayList<Table> tables = new ArrayList<>();
        // This query checks if there is an ACTIVE visit (is_active=1) on the table
        String sql = "SELECT t.table_number, t.capacity, t.is_active, v.visit_id, w.waiting_id " +
                     "FROM tables t " +
                     "LEFT JOIN visits v ON t.table_id = v.table_id AND v.is_active = 1 " +
                     "LEFT JOIN waiting_list w ON t.table_id = w.table_id AND w.status = 'notified' " +
                     "ORDER BY t.table_number ASC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Table t = new Table(
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getBoolean("is_active")
                );
                boolean isSeated = rs.getObject("visit_id") != null;
                boolean isReservedForWaitlist = rs.getObject("waiting_id") != null;   
                // If visit_id is not null or waiting_id not null, the table is occupied
                t.setOccupied(isSeated || isReservedForWaitlist);
                if(isSeated) {
                	t.setCurrentVisit(getVisit((Integer)rs.getObject("visit_id"), guiController));
                }
                tables.add(t);
            }
            return tables;
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching tables: " + e.getMessage());
        }
        return null;
    }
	//======================================
	//GET STAFF
	//======================================
	
	/**
	 * Get staff using his username from db
	 * @param username String, username of staff
	 * @param guiController logging controller
	 * @return on success Staff object, on failure null
	 */
	public static Staff getStaff(String username, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM staff WHERE username = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, username);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		boolean isManager = "manager".equalsIgnoreCase(rs.getString("role"));
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
	
	/**
	 * Get visit with visit id from db
	 * @param visitId Integer wanted visit id in db
	 * @param guiController logging controller
	 * @return on success Visit object, on failure null
	 */
	public static Visit getVisit(Integer visitId, ServerFrameController guiController) {
		return getVisitByField("visit_id", visitId, guiController);
	}
	
	/**
	 * Get visit from db by verification code of this visit
	 * @param verificationCode String wanted verification code from db
	 * @param guiController logging controller
	 * @return on success Visit object, on failure null
	 */
	public static Visit getVisitByVerificationCode(String verificationCode, ServerFrameController guiController) {
		return getVisitByField("verification_code", verificationCode, guiController);
	}
	/**
	 * Helper method to run get visit queries
	 * @param field field to look by in db
	 * @param value the object itself to look for 
	 * @param guiController logging controller
	 * @return on success Visit object, on failure null
	 */
	private static Visit getVisitByField(String field, Object value, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM visits WHERE " + field + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return constructVisitFromResultSet(rs, guiController);
                }
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching visit by " + field + ": " + e.getMessage());
        }
        return null;
    }
	
	/**
	 * Helper method to construct Visit object 
	 * @param rs ResultSet recieved by executing query to db
	 * @param guiController logging controller
	 * @return on success Visit object, on failure null
	 * @throws SQLException
	 */
	private static Visit constructVisitFromResultSet(ResultSet rs, ServerFrameController guiController) throws SQLException {
        Visit visit = null;
        Table assignedTable = getTable(rs.getInt("table_id"), guiController);
        Integer visitId = rs.getInt("visit_id");
        Integer memberId = (Integer)rs.getObject("member_id");
        
        // Determine type of visit source (Reservation vs Walk-in)
        if (rs.getObject("reservation_number") != null) { 
            Reservation assignedReservation = getReservation(rs.getInt("reservation_number"), guiController);
            visit = new Visit(assignedReservation, assignedTable);    
        } else if (memberId != null) {
            Member assignedMember = getMemberById(memberId, guiController);
            visit = new Visit(assignedMember, assignedTable);
            visit.setPartySize(rs.getInt("party_size"));
        } else {
            Guest assignedGuest = new Guest(rs.getString("guest_full_name"), rs.getString("guest_phone"), rs.getString("email"));
            visit = new Visit(assignedGuest, assignedTable);
            visit.setPartySize(rs.getInt("party_size"));
        }

        if (visit != null) {
            if (rs.getObject("start_time") != null) {
                LocalDateTime start = rs.getTimestamp("start_time").toLocalDateTime();
                visit.setStartTime(new DateTime(start.toLocalDate().toString(), start.toLocalTime().toString()));
            }
            if (rs.getObject("end_time") != null) {
                LocalDateTime end = rs.getTimestamp("end_time").toLocalDateTime();
                visit.setEndTime(new DateTime(end.toLocalDate().toString(), end.toLocalTime().toString()));
            }
            visit.setActive(rs.getBoolean("is_active"));
            visit.setVisitId(visitId);
            visit.setBillOfVisit(getBill(visitId, guiController));
            visit.setVerificationCode(rs.getString("verification_code"));
        }
        return visit;
    }
	
	/**
	 * Get waiting visit from waiting_list table in db
	 * @param verificationCode Sting verification code of visit wanted from db
	 * @param guiController logging controller
	 * @return on success Visit object, on failure null
	 */
	public static Visit getWaitingVisit(String verificationCode, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM waiting_list WHERE verification_code = ?";
	    Visit toReturn = null;
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, verificationCode);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		Integer memberId = (Integer) rs.getObject("member_id");
	        		Guest guest = new Guest(rs.getString("guest_full_name")
	        							,rs.getString("guest_phone")
	        							,rs.getString("email"));
	        		if(memberId != null) {
	        			Member member = getMemberById(memberId,guiController);
	        			toReturn = new Visit(member, null);
	        		} else {
	        			toReturn = new Visit(guest, null);
	        		}
	        		toReturn.setWaitingId(rs.getInt("waiting_id"));
	        		toReturn.setVerificationCode(rs.getString("verification_code"));
	        		toReturn.setPartySize(rs.getInt("number_of_guests"));
	            }
	        }
	      } catch (SQLException e) {
	       		guiController.addToConsole("Error fetching visit: " + e.getMessage());
	      }
	      return toReturn;
	}
	
	/**
	 * Get active visits that haven't recieved bill and seating at least 2 hours
	 * @param guiController logging controller
	 * @return on succes List of Visit object, on failure null
	 */
	public static List<Visit> getVisitsDueForBilling(ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        List<Visit> list = new ArrayList<>();

        String sql = "SELECT * FROM visits " +
                     "WHERE is_active = TRUE " +
                     "AND bill_sent = FALSE " +
                     "AND start_time < (NOW() - INTERVAL 2 HOUR)";

        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Visit v = getVisit(rs.getInt("visit_id"), guiController);
                if(v != null) list.add(v);
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching billing list: " + e.getMessage());
        }
        return list;
    }
	
	/**
	 * Get all visit for specific member using his member id
	 * @param memberId Integer member id wanted from db
	 * @param guiController logging controller
	 * @return List of Visit objects on success, on failre return null
	 */
	public static ArrayList<Visit> getMemberVisits(Integer memberId, ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    ArrayList<Visit> visits = new ArrayList<>();
	    
	    // Join visits with reservation to filter by member_id
	    String sql = "SELECT * FROM visits WHERE member_id = ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, memberId);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                visits.add(constructVisitFromResultSet(rs, guiController));
	            }
	        }
	    } catch (SQLException e) {
	        guiController.addToConsole("Error fetching member visits: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return visits;
	}
	
	/**
	 * Get active visits objects as List
	 * @param guiController logging controller
	 * @return on success List of Visits, on failure null
	 */
	public static ArrayList<Visit> getActiveVisits(ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    ArrayList<Visit> visits = new ArrayList<>();

	    String sql = "SELECT * FROM visits WHERE is_active = TRUE";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                visits.add(constructVisitFromResultSet(rs, guiController));
	            }
	        }
	    } catch (SQLException e) {
	        guiController.addToConsole("Error fetching active visits: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return visits;
	}
	//======================================
	//GET BILL
	//======================================
	
	/**
	 * Get bill for specific visit by visit id
	 * @param visitId visit id of wanted bill
	 * @param guiController logging controller
	 * @return on success Bill object, on failure null
	 */
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
	
	// ======================================
    // GET WAITING LIST
    // ======================================
	/**
	 * Get all waiting visits from db as List
	 * @param guiController logging controller
	 * @return List of visits on success, null on failure
	 */
    public static List<Visit> getWaitingList(ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        List<Visit> waitingList = new ArrayList<>();
        
        String sql = "SELECT * FROM waiting_list WHERE status IN ('waiting', 'notified') ORDER BY entered_at ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Visit visit = null;
                Integer memberId = (Integer) rs.getObject("member_id");
                
                Guest guest = new Guest(
                    rs.getString("guest_full_name"),
                    rs.getString("guest_phone"),
                    rs.getString("email")
                );

                //client or member
                if (memberId != null && memberId > 0) {
                     Member member = getMemberById(memberId, guiController);
                     visit = new Visit(member, null); 
                } else {
                     visit = new Visit(guest, null);
                }

                // fill fields for the waiting list
                visit.setWaitingId(rs.getInt("waiting_id"));
                visit.setVerificationCode(rs.getString("verification_code"));
                visit.setPartySize(rs.getInt("number_of_guests"));
                visit.setStatus(Status.valueOf(rs.getString("status")));
                waitingList.add(visit);
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching waiting list: " + e.getMessage());
        }
        return waitingList;
    }
	
    /**
     * Get all waiting visits that haven't arrive for 15 mins after being notified bout their table being ready
     * @param guiController logging controller
     * @return on success List of visit objects, on failure null.
     */
    public static List<Visit> getExpiredWaitingListEntries(ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        List<Visit> expired = new ArrayList<>();

        String sql = "SELECT * FROM waiting_list " +
                     "WHERE status = 'notified' " +
                     "AND notified_at < (NOW() - INTERVAL 15 MINUTE)";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer memberId = (Integer) rs.getObject("member_id");
                Guest guest = new Guest(
                    rs.getString("guest_full_name"),
                    rs.getString("guest_phone"),
                    rs.getString("email")
                );

                Visit visit;
                if (memberId != null && memberId > 0) {
                    Member member = getMemberById(memberId, guiController);
                    visit = new Visit(member, null);
                } else {
                    visit = new Visit(guest, null);
                }
                
                visit.setWaitingId(rs.getInt("waiting_id"));
                visit.setVerificationCode(rs.getString("verification_code"));
                
                expired.add(visit);
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching expired waiting list: " + e.getMessage());
        }
        return expired;
    }
   /**
    * 
    * @param guiController
    * @return
    */
    public static List<Member> getAllMembers(ServerFrameController guiController) {
        List<Member> members = new ArrayList<>();
        Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM members";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Member m = new Member(
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                m.setMemberId(rs.getInt("member_id"));
                m.setCardCode(rs.getString("card_code"));
                // Add any other fields if needed
                
                members.add(m);
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching all members: " + e.getMessage());
            e.printStackTrace();
        }
        return members;
    }
    
    public static RestaurantConfig getRestaurantConfig(ServerFrameController guiController) {
        RestaurantConfig config = new RestaurantConfig();
        Connection conn = dbController.getInstance().getConnection();
        
        try {
            // Fetch Regular Hours
            String sqlReg = "SELECT * FROM regular_hours";
            try (PreparedStatement ps = conn.prepareStatement(sqlReg); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    config.getRegularHours().put(rs.getString("day_name"), 
                            new String[]{rs.getString("open_time"), rs.getString("close_time")});
                }
            }

            // Fetch Special Hours 
            String sqlSpec = "SELECT * FROM special_hours";
            try (PreparedStatement ps = conn.prepareStatement(sqlSpec); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("special_date");
                    if (sqlDate != null) {
                        config.getSpecialHours().put(sqlDate.toLocalDate(), 
                                new String[]{rs.getString("open_time"), rs.getString("close_time")});
                    }
                }
            }

        } catch (SQLException e) {
            guiController.addToConsole("Error getting config: " + e.getMessage());
        }
        return config;
    }
    
    // ======================================
    //RETRIEVE ALL RESERVATIONS
    // ======================================
    public static List<Reservation> getAllReservations(ServerFrameController guiController) {
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = dbController.getInstance().getConnection();
        // Retrieving all orders, sorted by date and time
        String sql = "SELECT * FROM reservation ORDER BY reservation_date DESC, reservation_time DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation res = new Reservation(
                    rs.getInt("reservation_number"),
                    new DateTime(rs.getString("reservation_date"), rs.getString("reservation_time")),
                    rs.getString("verification_code"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("member_id"),
                    new Guest(
                        rs.getString("guest_full_name"),
                        rs.getString("guest_phone"),
                        rs.getString("email")
                    )
                );
                
                // Convert status from string to Enum
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    res.setStatus(Status.valueOf(statusStr));
                }
                res.setDateOfPlacingReservation(rs.getString("created_at"));
                
                reservations.add(res);
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching all reservations: " + e.getMessage());
        }
        return reservations;
    }
    
    // Get all reservations for a specific member by member ID
    public static List<Reservation> getReservationsByMemberId(Integer memberId, ServerFrameController guiController) {
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM reservation WHERE member_id = ? ORDER BY reservation_date DESC, reservation_time DESC";
        // Retrieve all orders associated with this member id
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                	// Using the existing helper function to map the row to an object
                    reservations.add(mapRowToReservation(rs, guiController));
                }
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching reservations by member ID: " + e.getMessage());
        }
        return reservations;
    }
}