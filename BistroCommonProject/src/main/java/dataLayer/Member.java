package dataLayer;


public class Member extends Guest {
	private String userName;
	private String password;
	private Integer memberId;
	//private List<Visit> visitHistory;
	
	private int memberCounter;
	
	public Member (Integer phoneNumber, String email, String userName, String password, Integer memberId) {
		super(phoneNumber, email);
		this.userName = userName;
		this.password = password;
		setMemberId(memberId);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public int getMemberCounter() {
		return memberCounter;
	}

	public void setMemberCounter(int memberCounter) {
		this.memberCounter = memberCounter;
	}
}
