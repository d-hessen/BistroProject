package dataLayer;


public class Member extends Guest {
	private String password;
	private Integer memberId;
	private String cardCode;
	
	public Member (String fullName, String phoneNumber, String email, String password) {
		super(fullName, phoneNumber, email);
		this.password = password;
		setCardCode(cardCode);
	}

	public String getCardCode() {
		return cardCode;
	}

	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}
}
