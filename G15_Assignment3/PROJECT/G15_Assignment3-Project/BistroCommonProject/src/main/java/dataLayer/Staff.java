package dataLayer;

import java.io.Serializable;

/**
 * Represents a staff member in the system.
 * Includes authentication details and role information.
 */
public class Staff implements Serializable {

	/**
     * Unique identifier of the staff member.
     */
    private Integer staffId;

    /**
     * Username used for staff login.
     */
    private String username;

    /**
     * Password used for staff authentication.
     */
    private String password;

    /**
     * Full name of the staff member.
     */
    private String fullName;

    /**
     * Indicates whether the staff member is a manager.
     */
    private boolean isManager;

    /**
     * Creates a new Staff object.
     *
     * @param username staff username
     * @param password staff password
     * @param fullName staff full name
     * @param isManager true if manager, false if regular worker
     */
	public Staff(String username, String password, String fullName, boolean isManager) {
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.isManager = isManager;
	}
	
	/**
     * Returns the staff ID.
     *
     * @return staff ID
     */
	public Integer getStaffId() {
		return staffId;
	}

	/**
     * Sets the staff ID.
     *
     * @param staffId staff ID
     */
	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}

    /**
     * Returns the username.
     *
     * @return username
     */
	public String getUsername() {
		return username;
	}

	/**
     * Sets the username.
     *
     * @param username staff username
     */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
     * Returns the password.
     *
     * @return password
     */
	public String getPassword() {
		return password;
	}
	
	/**
     * Sets the password.
     *
     * @param password staff password
     */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
     * Returns the full name of the staff member.
     *
     * @return full name
     */
	public String getFullName() {
		return fullName;
	}
	
	/**
     * Sets the full name of the staff member.
     *
     * @param fullName staff full name
     */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	/**
     * Indicates whether the staff member is a manager.
     *
     * @return true if manager, false otherwise
     */
	public boolean isManager() {
		return isManager;
	}
	
	/**
     * Sets the manager role of the staff member.
     *
     * @param isManager true if manager, false otherwise
     */
	public void setManager(boolean isManager) {
		this.isManager = isManager;
	}
	
	/**
     * Returns the role of the staff member.
     *
     * @return "manager" or "worker"
     */
	public String getRole() {
		if(isManager) return "manager";
		else {
			return "worker";
		}
	}
	

}
