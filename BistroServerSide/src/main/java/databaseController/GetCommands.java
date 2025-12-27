package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dataLayer.DateTime;
import dataLayer.Guest;
import dataLayer.Member;
import dataLayer.Reservation;
import dataLayer.Table;
import dataLayer.Visit;
import domainLogic.ServerFrameController;
import common.Status;

public class GetCommands {
	//======================================
	//GET OPERATIONS WITH RESERVATION
	//======================================
	//Retrieve a reservation by ID
    public static Reservation getReservation(Integer id, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO GET RESERVATION CHECK FIELDS IN DATABASE BEFORE CHANGE
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

	//======================================
	//GET GUEST
	//======================================
	//Get guest by phone number
	public Guest getGuest(Integer phone, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
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
	       		guiController.addToConsole("Error fetching guest: " + e.getMessage());
	        }
	        return null;
	}
	
	//Get guest by email
	public Guest getGuest(String email, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		        
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
		    	 guiController.addToConsole("Error fetching guest: " + e.getMessage());
		      }
		     return null;
	}
	
	//======================================
	//GET MEMBER
	//======================================
	//Get member by phone number
	public Member getMember(Integer phone, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
	    String sql = "SELECT * FROM members WHERE phone = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setInt(1, phone);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		return new Member(
	        				rs.getString("full_name"),
	                        rs.getString("phone"),
	                        rs.getString("email"),
	                        rs.getString("password")
	                    );
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return null;
	}
	//Get member by email
	public Member getMember(String email, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	        
	    String sql = "SELECT * FROM members WHERE email = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	    	ps.setString(1, email);
	        try (ResultSet rs = ps.executeQuery()) {
	        	if (rs.next()) {
	        		return new Member(
	        				rs.getString("full_name"),
	                        rs.getString("phone"),
	                        rs.getString("email"),
	                        rs.getString("password")
	                    );
	                }
	            }
	       	} catch (SQLException e) {
	       		guiController.addToConsole("Error fetching member: " + e.getMessage());
	        }
	        return null;
	}
	
	//======================================
	//GET TABLE
	//======================================
	//Get table
	public Table getTable(Integer tableId, ServerFrameController guiController) {
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
	
	//======================================
	//GET VISIT
	//======================================
	//Get visit by visitId
	public Visit getVisit(Integer visitId, ServerFrameController guiController) {
			Connection conn = dbController.getInstance().getConnection();
		        
		    String sql = "SELECT * FROM visits WHERE visit_id = ?";
		        
		    try (PreparedStatement ps = conn.prepareStatement(sql)) {
		    	ps.setInt(1, visitId);
		        try (ResultSet rs = ps.executeQuery()) {
		        	if (rs.next()) {
		        		return new Visit(
		        				rs.getInt("table_id"),
		        				rs.getInt("reservation_number"),
		        				rs.getInt("waiting_id"),
		        				rs.getString("start_time"),
		        				rs.getString("end_time"),
		        				rs.getBoolean("is_active")
		                    );
		                }
		            }
		       	} catch (SQLException e) {
		       		guiController.addToConsole("Error fetching table: " + e.getMessage());
		        }
		        return null;
	}
	
	//======================================
	//GET BILL
	//======================================
	//Get bill by visitId (NEED TO WRITE)
}
