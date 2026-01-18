package dataLayer;

import java.io.Serializable;

/**
 * Represents a table in the restaurant.
 * Holds capacity, status, and current visit information.
 */
public class Table implements Serializable {

	/**
     * Unique table number.
     */
    private Integer tableNumber;

    /**
     * Maximum number of guests allowed at the table.
     */
    private Integer tableCapacity;
    
    /**
     * Indicates whether the table is active in the system.
     */
	private boolean isActive = false;

	/**
     * Indicates whether the table is currently occupied.
     */
    private boolean isOccupied;

    /**
     * The current visit assigned to the table, if any.
     */
    private Visit currentVisit;
	
    /**
     * Creates a new table instance.
     *
     * @param tableNumber table identifier
     * @param tableCapacity maximum number of guests
     * @param isActive initial active status
     */
	public Table(Integer tableNumber, Integer tableCapacity, boolean isActive) {
		this.isActive = isActive;
		this.tableNumber = tableNumber;
		this.tableCapacity = tableCapacity;
		this.isOccupied = false;
	}

	/**
     * Returns the table number.
     *
     * @return table number
     */
	public Integer getTableNumber() {
		return tableNumber;
	}

	/**
     * Sets the table number.
     *
     * @param tableNumber table identifier
     */
	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	/**
     * Returns the table capacity.
     *
     * @return table capacity
     */
	public Integer getTableCapacity() {
		return tableCapacity;
	}

	/**
     * Sets the table capacity.
     *
     * @param tableCapacity maximum number of guests
     */
	public void setTableCapacity(int tableCapacity) {
		this.tableCapacity = tableCapacity;
	}

	/**
     * Indicates whether the table is active.
     *
     * @return true if active, false otherwise
     */
	public boolean isActive() {
		return isActive;
	}

	/**
     * Sets the active status of the table.
     *
     * @param isActive true to activate the table
     */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
     * Indicates whether the table is currently occupied.
     *
     * @return true if occupied, false otherwise
     */
	public boolean isOccupied() {
		return isOccupied;
	}

	/**
     * Sets the occupied status of the table.
     *
     * @param isOccupied true if occupied
     */
	public void setOccupied(boolean isOccupied) {
		this.isOccupied = isOccupied;
	}

	/**
     * Returns the current visit assigned to the table.
     *
     * @return current visit or null
     */
	public Visit getCurrentVisit() {
		return currentVisit;
	}

	/**
     * Assigns a visit to the table.
     *
     * @param currentVisit visit assigned to the table
     */
	public void setCurrentVisit(Visit currentVisit) {
		this.currentVisit = currentVisit;
	}
	
	
}
