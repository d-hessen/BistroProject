package dataLayer;

public class Table {
	private int tableNumber;
	private int tableCapacity;
	private boolean isActive = false;
	
	public Table(int tableNumber, int tableCapacity, boolean isActive) {
		this.isActive = isActive;
		this.tableNumber = tableNumber;
		this.tableCapacity = tableCapacity;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public int getTableCapacity() {
		return tableCapacity;
	}

	public void setTableCapacity(int tableCapacity) {
		this.tableCapacity = tableCapacity;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	

}
