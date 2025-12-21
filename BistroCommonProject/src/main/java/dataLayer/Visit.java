package dataLayer;

public class Visit {
	private int tableId;
	private int reservationId;
	private int waitingId;
	private String startTime;
	private String endTime;
	private boolean isActive;
	
	public Visit(int tableId, int reservationId, int waitingId, String startTime, String endTime, boolean isActive) {
		this.tableId = tableId;
		this.reservationId = reservationId;
		this.waitingId = waitingId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isActive = isActive;
	}
	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	public int getReservationId() {
		return reservationId;
	}

	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}

	public int getWaitingId() {
		return waitingId;
	}

	public void setWaitingId(int waitingId) {
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
