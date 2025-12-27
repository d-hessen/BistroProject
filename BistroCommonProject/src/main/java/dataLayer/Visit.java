package dataLayer;

public class Visit {
	private Integer tableId;
	private Integer reservationId;
	private Integer waitingId;
	private String startTime;
	private String endTime;
	private boolean isActive;
	
	public Visit(Integer tableId, Integer reservationId, Integer waitingId, String startTime, String endTime, boolean isActive) {
		this.tableId = tableId;
		this.reservationId = reservationId;
		this.waitingId = waitingId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isActive = isActive;
	}
	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public Integer getWaitingId() {
		return waitingId;
	}

	public void setWaitingId(Integer waitingId) {
		this.waitingId = waitingId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
