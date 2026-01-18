package dataLayer;

import java.io.Serializable;

/**
 * Represents a simple date and time pair.
 * Used for reservations, visits, and billing timestamps.
 */
public class DateTime implements Serializable {

	/** Date value (format: yyyy-MM-dd). */
    private String date;

    /** Time value (format: HH:mm or HH:mm:ss). */
    private String time;
	
    /**
     * Creates a DateTime object with date and time.
     *
     * @param date the date string
     * @param time the time string
     */
	public DateTime(String date, String time) {
		this.date = date;
		this.time = time;
	}

	/**
     * Returns the date value.
     *
     * @return date string
     */
	public String getDate() {
		return date;
	}

	/**
     * Sets the date value.
     *
     * @param date the date string
     */
	public void setDate(String date) {
		this.date = date;
	}

	/**
     * Returns the time value.
     *
     * @return time string
     */
	public String getTime() {
		return time;
	}

	/**
     * Sets the time value.
     *
     * @param time the time string
     */
	public void setTime(String time) {
		this.time = time;
	}

	/**
     * Returns a formatted string representation of the date and time.
     *
     * @return formatted date-time string
     */
	@Override
	public String toString() {
		return time + "("+date+")";
	}

}
