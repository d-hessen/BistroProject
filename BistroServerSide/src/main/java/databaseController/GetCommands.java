package databaseController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
					Reservation res = mapRowToReservation(rs);
					if (res.getMemberId() != null && res.getMemberId() > 0) {
						Guest fullMember = getMemberById(res.getMemberId(), guiController);
						if (fullMember != null) {
							res.setGuest(fullMember);
						}
					}
					return res;
				} else {
					guiController.addToConsole("Reservation not found for ID: " + id);
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservation: " + e.getMessage());
		}
		return null;
	}
  	//Method that gets reservation by member card code
    public static Reservation findUpcomingReservationByCode(String cardCode, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		int memberId = -1;
		
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
						return mapRowToReservation(rs); // Found valid reservation
					}
				}
			}
		} catch (Exception e) {
			guiController.addToConsole("Error finding reservation: " + e.getMessage());
		}
		return null;
	}
    //Get reservations between start time and end time for today
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
					upcoming.add(mapRowToReservation(rs));
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching upcoming reservations: " + e.getMessage());
		}
		return upcoming;
	}
	// Retrieve reservations by phone number
	public static List<Reservation> getReservationsByPhoneNumber(String phoneNumber, ServerFrameController guiController) {
		List<Reservation> reservations = new ArrayList<>();
		Connection conn = dbController.getInstance().getConnection();
		String sql = "SELECT * FROM reservation WHERE guest_phone = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, phoneNumber);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					reservations.add(mapRowToReservation(rs));
				}
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservations by phone: " + e.getMessage());
		}

		if (reservations.isEmpty()) {
			guiController.addToConsole("No reservations found for phone number: " + phoneNumber);
		}

		return reservations;
	}
	//Reservations that are late for more than 15 mins
	public static List<Reservation> getExpiredReservations(ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		List<Reservation> expiredList = new ArrayList<>();
		
		String sql = "SELECT * FROM reservation " +
					 "WHERE status = 'approved' " +
					 "AND TIMESTAMP(CONCAT(reservation_date, ' ', reservation_time)) < NOW() - INTERVAL 15 MINUTE";

		try (PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				expiredList.add(mapRowToReservation(rs));
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching expired reservations: " + e.getMessage());
		}
		return expiredList;
	}
	//Upcoming reservations to remind
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
				reminders.add(mapRowToReservation(rs));
			}
		} catch (SQLException e) {
			guiController.addToConsole("Error fetching reservations for reminder: " + e.getMessage());
		}
		return reminders;
	}
	//Get reservations by phone or email
    public static List<Reservation> getReservationsByContactInfo(String contactInput, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        List<Reservation> list = new ArrayList<>();

        String sql = "SELECT * FROM reservation WHERE guest_phone = ? OR email = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactInput);
            ps.setString(2, contactInput);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error fetching by contact info: " + e.getMessage());
        }
        return list;
    }
	//Helper method to construct reservation
	private static Reservation mapRowToReservation(ResultSet rs) throws SQLException {
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

		try {
			res.setStatus(Status.valueOf(rs.getString("status")));
		} catch (IllegalArgumentException | NullPointerException e) {
			res.setStatus(Status.pending);
		}
		
		res.setDateOfPlacingReservation(rs.getString("created_at"));
		res.setVerificationCode(rs.getString("verification_code")); 
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
	//Get member by phone number
	public static Member getMember(Integer phone, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
	    String sql = "SELECT * FROM members WHERE phone = ?";
	    Member toReturn = null;    
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, phone);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		toReturn = constructMemberFromResultSet(rs, conn, guiController);
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return toReturn;
	}
	//Get member by email
	public static Member getMember(String email, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
	    String sql = "SELECT * FROM members WHERE email = ?";
	    Member toReturn = null;
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, email);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		toReturn = constructMemberFromResultSet(rs, conn, guiController);
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return toReturn;
	}
	//Get member by card code
	public static Member getMemberByCode(String code, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "SELECT * FROM members WHERE card_code = ?";
        Member toReturn = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                	toReturn = constructMemberFromResultSet(rs, conn, guiController);
                }
            }
        } catch (Exception e) {}
        return toReturn;
    }
	//Get member by memberId
	public static Member getMemberById(Integer memberId, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM members WHERE member_id = ?";
	    Member toReturn = null;
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, memberId);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		toReturn = constructMemberFromResultSet(rs, conn, guiController);
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return toReturn;
	}
	//Helper method to construct visit
	private static Member constructMemberFromResultSet(ResultSet rs, Connection conn, ServerFrameController guiController) throws SQLException {
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
	//Fetch all tables and check if they are currently occupied
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
	//Get reservation with verificationCode
	public static Reservation getReservationVerificationCode(String codeStr, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();

		String sql = "SELECT * FROM reservation WHERE verification_code = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, codeStr); 
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Reservation toReturn = new Reservation(
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
					toReturn.setVerificationCode(rs.getString("verification_code"));
					
					if (rs.getString("status") != null) {
						toReturn.setStatus(Status.valueOf(rs.getString("status")));
					}
					
					toReturn.setDateOfPlacingReservation(rs.getString("created_at"));
					
					return toReturn;
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
	            }
	        }
	      } catch (SQLException e) {
	       		guiController.addToConsole("Error fetching visit: " + e.getMessage());
	      }
	      return toReturn;
	}
	//Get visit by verification code from visits table 
	public static Visit getVisitByVerificationCode(String verificationCode, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
        
	    String sql = "SELECT * FROM visits WHERE verification_code = ?";
	    Visit toReturn = null;
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, verificationCode);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		toReturn = constructVisitFromResultSet(rs, conn, guiController);
	            }
	        }
	      } catch (SQLException e) {
	       		guiController.addToConsole("Error fetching visit: " + e.getMessage());
	      }
	      return toReturn;
	}
	//Get visit by verification code from waiting list
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
	//Get all visits for specific member
	public static ArrayList<Visit> getMemberVisits(Integer memberId, ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    ArrayList<Visit> visits = new ArrayList<>();
	    
	    // Join visits with reservation to filter by member_id
	    String sql = "SELECT * FROM visits WHERE member_id = ?";

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
	    Table assignedTable = getTable(rs.getInt("table_id"), guiController);
	    Integer visitId = rs.getInt("visit_id");
	    Integer memberId = (Integer)rs.getObject("member_id");
		if(rs.getObject("reservation_number") != null) { //If visit were reserved
			Reservation assignedReservation = getReservation((Integer)rs.getObject("reservation_number"),guiController);
			visit = new Visit(assignedReservation, assignedTable);	
		}
		else if(memberId != null) {
			Member assignedMember = (Member)getMemberById(rs.getInt("member_id"), guiController);
			visit = new Visit(assignedMember, assignedTable);
			visit.setPartySize(rs.getInt("party_size"));
		}
		else {
			Guest assignedGuest = getGuest(rs.getInt("waiting_id"), guiController);
			visit = new Visit(assignedGuest, assignedTable);
			visit.setPartySize(rs.getInt("party_size"));
		}
		if(visit != null) {
			//Timestamps of start and end visit
			if(rs.getObject("start_time") != null) {
				LocalDateTime startDateTime = rs.getTimestamp("start_time").toLocalDateTime();
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String startTime = startDateTime.format(timeFormatter);
				String startDate = startDateTime.format(dateFormatter);	
				visit.setStartTime(new DateTime(startDate, startTime));
			}
			if(rs.getObject("end_time") != null) {
				LocalDateTime endDateTime = rs.getTimestamp("end_time").toLocalDateTime();
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String endTime = endDateTime.format(timeFormatter);
				String endDate = endDateTime.format(dateFormatter);	
				visit.setEndTime(new DateTime(endDate, endTime));
			}
			visit.setActive(rs.getBoolean("is_active"));
			visit.setVisitId(visitId);
			visit.setBillOfVisit(getBill(visitId, guiController));
        	visit.setVerificationCode(rs.getString("verification_code"));
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
	
	// ======================================
    // GET WAITING LIST
    // ======================================
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
    
	public static List<Table> getAllActiveTables(ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		List<Table> tables = new ArrayList<>();
		String sql = "SELECT table_number, capacity, is_active " + "FROM tables " + "WHERE is_active = 1";
		
		try (PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
	            tables.add(new Table(
	                    rs.getInt("table_number"),
	                    rs.getInt("capacity"),
	                    rs.getBoolean("is_active")));
			}
		} catch (SQLException e) {
	        guiController.addToConsole("Error fetching active tables: " + e.getMessage());
	        e.printStackTrace();
	    }
		return tables;
	}
	
	public static List<Reservation> getReservationOverlap(LocalDateTime windowStart, LocalDateTime windowEnd, ServerFrameController guiController) {
	    Connection conn = dbController.getInstance().getConnection();
	    List<Reservation> list = new ArrayList<>();

	    String sql = "SELECT * "
	            + "FROM reservation "
	            + "WHERE status NOT IN ('cancelled', 'no_show') "
	            + "AND CONCAT(reservation_date, ' ', reservation_time) < ? "
	            + "AND DATE_ADD(CONCAT(reservation_date, ' ', reservation_time), INTERVAL 2 HOUR) > ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        ps.setString(1, windowEnd.format(formatter)); 
	        ps.setString(2, windowStart.format(formatter));
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	              
	                Integer memberId = (Integer) rs.getObject("member_id");
	                String fullName = rs.getString("guest_full_name");
	                String phone = rs.getString("guest_phone");
	                String email = rs.getString("email");
	                Guest guest = null;
	                if (fullName != null || phone != null || email != null) {
	                    guest = new Guest(fullName, phone, email);
	                }
	                Reservation r = new Reservation(
	                        rs.getInt("reservation_number"),
	                        new DateTime(rs.getString("reservation_date"), rs.getString("reservation_time")),
	                        rs.getString("verification_code"),
	                        rs.getInt("number_of_guests"),
	                        memberId,
	                        guest);

	                String status = rs.getString("status");
	                if (status != null) {
	                    r.setStatus(Status.valueOf(status));
	                }
	                String code = (String)rs.getObject("verification_code");
	                if (code != null) {
	                    r.setVerificationCode(code);
	                }
	                list.add(r);
	            }
	        }
	    } catch (SQLException e) {
	        guiController.addToConsole("Error fetching overlapping reservations: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return list;
	}
    
    // ======================================
    //RETRIEVE ALL MEMBERS
    // ======================================
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
}