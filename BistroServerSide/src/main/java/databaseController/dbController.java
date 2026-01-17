package databaseController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/*
 * Singleton controller to manage database connection.
 * Handles connection establishment to MySQL local database
 * */
public final class dbController {
	private static dbController instance;
	private static Connection conn;
	
	//Database Configuration
	private static final String dbUrl = "jdbc:mysql://localhost:3306/bistro?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
	private static final String dbUser = "root";
	private static final String dbPass = "ZbangZod1234@#$qwe"; 
	
	/*
	 * Constructor for Singleton pattern
	 * Automatically attempts to connect to DB when called
	 * */
	private dbController(){
		connectToDB();
	}
	
	/*
	 * Get single instance of dbController
	 * @return The singleton instance of dbController
	 * */
	public static synchronized dbController getInstance() {
        if (instance == null) {
            instance = new dbController();
        }
        return instance;
    }
	
	/*
	 * return active db connection
	 * @return generic SQL connection object
	 * */
    public Connection getConnection() {
        return conn;
    }
	
    /*
     * Establish connection to db.
     * @return true if connection succeeded. flase otherwise.
     * */
	public boolean connectToDB() {
		try { 
	        conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
	        return true;
	    }
	    catch(SQLException ex) {
	    	System.err.println("Database Connection Error:");
            System.err.println("Message: " + ex.getMessage());
            System.err.println("SQLState: " + ex.getSQLState());
            System.err.println("VendorError: " + ex.getErrorCode());
            return false;
	    }
	}
	
	//Closes db connection and reset singleton instance
    public void disconnectFromDB() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            instance = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
