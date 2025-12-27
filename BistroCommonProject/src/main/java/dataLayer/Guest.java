package dataLayer;

import java.io.Serializable;

public class Guest implements Serializable {
	private String phoneNumber;
	private String fullName;
	private String email;
	
	//Guest can insert phone number/email/both
	public Guest (String fullName, String phoneNumber, String email) {
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
