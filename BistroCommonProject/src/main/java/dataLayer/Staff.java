package dataLayer;

public class Staff {
	private String username;
	private String password;
	private String fullName;
	private boolean isManager;
	
	public Staff(String username, String password, String fullName, boolean isManager) {
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.isManager = isManager;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public boolean isManager() {
		return isManager;
	}
	public void setManager(boolean isManager) {
		this.isManager = isManager;
	}
	

}
