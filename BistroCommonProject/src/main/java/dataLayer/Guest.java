package dataLayer;

import java.io.Serializable;

/**
 * Represents a guest customer.
 * A guest may provide a phone number, email, or both.
 */
public class Guest implements Serializable {

	/** Guest phone number (optional). */
    private String phoneNumber;

    /** Guest full name. */
    private String fullName;

    /** Guest email address (optional). */
    private String email;
	
    /**
     * Creates a new Guest instance.
     *
     * @param fullName    the guest's full name
     * @param phoneNumber the guest's phone number (optional)
     * @param email       the guest's email address (optional)
     */
	public Guest (String fullName, String phoneNumber, String email) {
		super();
		this.phoneNumber = phoneNumber;
		this.fullName = fullName;
		this.email = email;
	}

	/**
     * Returns the guest's email address.
     *
     * @return email address
     */
	public String getEmail() {
		return email;
	}

	/**
     * Sets the guest's email address.
     *
     * @param email the email address
     */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
     * Returns the guest's phone number.
     *
     * @return phone number
     */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
     * Sets the guest's phone number.
     *
     * @param phoneNumber the phone number
     */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
     * Returns the guest's full name.
     *
     * @return full name
     */
	public String getFullName() {
		return fullName;
	}

	/**
     * Sets the guest's full name.
     *
     * @param fullName the full name
     */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
