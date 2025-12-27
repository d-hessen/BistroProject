package handlers;

public class MonthItem {

    private final int monthNumber;
    private final String monthName;

    public MonthItem(int monthNumber, String monthName) {
        this.monthNumber = monthNumber;
        this.monthName = monthName;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    public String getMonthName() {
        return monthName;
    }

    @Override
    public String toString() {
        return monthNumber + " - " + monthName;
    }
}
