package dataLayer;

/**
 * Represents a registered member.
 * Extends Guest with authentication and membership details.
 */
public class Member extends Guest {

	/** Member password (used for authentication). */
    private String password;

    /** Unique member identifier. */
    private Integer memberId;

    /** Digital membership card code. */
    private String cardCode;
	
    /**
     * Creates a new Member instance.
     *
     * @param fullName    member full name
     * @param phoneNumber member phone number
     * @param email       member email address
     * @param password    member password
     */
	public Member (String fullName, String phoneNumber, String email, String password) {
		super(fullName, phoneNumber, email);
		this.password = password;
		setCardCode(cardCode);
	}

	/**
     * Returns the member's card code.
     *
     * @return card code
     */
	public String getCardCode() {
		return cardCode;
	}

	/**
     * Sets the member's card code.
     *
     * @param cardCode the card code
     */
	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	/**
     * Returns the member's password.
     *
     * @return password
     */
	public String getPassword() {
		return password;
	}

	/**
     * Sets the member's password.
     *
     * @param password the password
     */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
     * Returns the member ID.
     *
     * @return member ID
     */
	public Integer getMemberId() {
		return memberId;
	}

	/**
     * Sets the member ID.
     *
     * @param memberId the member ID
     */
	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}
}
