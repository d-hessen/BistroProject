package dataLayer;

import java.io.Serializable;

public class WaitingHistoryItem implements Serializable {
    private Integer memberId;
    private String memberName;
    private String enteredAt; //Time entered waiting list
    private String status; //seated, cancelled, etc.

    public WaitingHistoryItem(Integer memberId, String memberName, String enteredAt, String status) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.enteredAt = enteredAt;
        this.status = status;
    }

    public Integer getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getEnteredAt() { return enteredAt; }
    public String getStatus() { return status; }
}