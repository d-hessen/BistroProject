package dataLayer;

import java.io.Serializable;

public class Table implements Serializable {
	private Integer tableNumber;
	private Integer tableCapacity;
	private boolean isActive = false;
	
	public Table(Integer tableNumber, Integer tableCapacity, boolean isActive) {
		this.isActive = isActive;
		this.tableNumber = tableNumber;
		this.tableCapacity = tableCapacity;
	}

	public Integer getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public Integer getTableCapacity() {
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
