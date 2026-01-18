package dataLayer;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;

/**
 * Holds restaurant opening hours configuration.
 * Includes regular weekly hours and special date specific hours.
 */
public class RestaurantConfig implements Serializable {

	/**
     * Regular opening hours by day name.
     * Key: day name (e.g., "Sunday"),
     * Value: array with opening and closing times.
     */
	private HashMap<String, String[]> regularHours; 
    
	/**
     * Special opening hours for specific dates.
     * Key: specific date,
     * Value: array with opening and closing times.
     */
	private HashMap<LocalDate, String[]> specialHours;

	/**
     * Creates a new RestaurantConfig instance.
     */
    public RestaurantConfig() {
        this.regularHours = new HashMap<>();
        this.specialHours = new HashMap<>();
    }

    /**
     * Returns the regular weekly opening hours.
     *
     * @return map of regular opening hours
     */
    public HashMap<String, String[]> getRegularHours() {
        return regularHours;
    }

    /**
     * Sets the regular weekly opening hours.
     *
     * @param regularHours map of regular opening hours
     */
    public void setRegularHours(HashMap<String, String[]> regularHours) {
        this.regularHours = regularHours;
    }

    /**
     * Returns the special opening hours.
     *
     * @return map of special opening hours
     */
    public HashMap<LocalDate, String[]> getSpecialHours() {
        return specialHours;
    }

    /**
     * Sets the special opening hours.
     *
     * @param specialHours map of special opening hours
     */
    public void setSpecialHours(HashMap<LocalDate, String[]> specialHours) {
        this.specialHours = specialHours;
    }
}