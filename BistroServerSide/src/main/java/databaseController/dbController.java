package databaseController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class dbController {
	private static dbController instance;
	private static Connection conn; 
	
	private dbController(){
		connectToDB();
	}
	
	//Public method to get the single instance.
	public static synchronized dbController getInstance() {
        if (instance == null) {
            instance = new dbController();
        }
        return instance;
    }
	
	//The Domain Controllers will call this method to run their queries.
    public Connection getConnection() {
        return conn;
    }
	
    //Establishes the connection to the database.
	public boolean connectToDB() {
		try {
			String dbUrl = "jdbc:mysql://localhost:3306/bistro?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
            String dbUser = "root";
            String dbPass = "Oriko12321"; 

            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            return true;
		  }
		  catch(SQLException ex) {
			  System.out.println("SQLException: " + ex.getMessage());
	          System.out.println("SQLState: " + ex.getSQLState());
	          System.out.println("VendorError: " + ex.getErrorCode());
	          return false;
		  }
	}
	
	//Closes the connection.
    public void disconnectFromDB() {
        try {
            if (conn != null) {
                conn.close();
                instance = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
