package dataLayer;


public class Member extends Guest {
	private String fullName;
	private String password;
	private Integer memberId;
	
	public Member (Integer phoneNumber, String email, String fullName, String password, Integer memberId) {
		super(phoneNumber, email, fullName);
		this.password = password;
		setMemberId(memberId);
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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
