package dataLayer;

import java.io.Serializable;

public class Guest implements Serializable {
	private Integer phoneNumber;
	private String fullName;
	private String email;
	
	
	public Guest (Integer phoneNumber, String email, String fullName) {
		super();
		this.phoneNumber = phoneNumber;
		this.fullName = fullName;
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Integer phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
