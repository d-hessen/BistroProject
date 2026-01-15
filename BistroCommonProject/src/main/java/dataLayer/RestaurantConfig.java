package dataLayer;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;

public class RestaurantConfig implements Serializable {
    // Key: Day name (Sunday), Value: String[]{open, close}
    private HashMap<String, String[]> regularHours; 
    
    // Key: Date, Value: String[]{open, close}
    private HashMap<LocalDate, String[]> specialHours;

    public RestaurantConfig() {
        this.regularHours = new HashMap<>();
        this.specialHours = new HashMap<>();
    }

    public HashMap<String, String[]> getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(HashMap<String, String[]> regularHours) {
        this.regularHours = regularHours;
    }

    public HashMap<LocalDate, String[]> getSpecialHours() {
        return specialHours;
    }

    public void setSpecialHours(HashMap<LocalDate, String[]> specialHours) {
        this.specialHours = specialHours;
    }
}