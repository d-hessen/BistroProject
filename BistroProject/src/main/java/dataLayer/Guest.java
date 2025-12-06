package dataLayer;

import java.io.Serializable;

public class Guest implements Serializable {
	private Integer phoneNumber;
	private String email;
	
	
	
	public Guest (Integer phoneNumber, String email) {
		super();
		this.phoneNumber = phoneNumber;
		this.email = email;
	}

	public Integer getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Integer phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
