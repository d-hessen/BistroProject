package handlers;

/**
 * Utility class for managing a visit countdown session.
 * Responsible for tracking visit duration and remaining time.
 */
public class VisitSessionManager {

	/** Target end time of the visit session (milliseconds). */
    private static Long targetEndTime;

    /** Indicates whether the visit has started. */
    private static boolean visitStarted;

    /**
     * Checks whether there is an active countdown timer.
     *
     * @return true if a timer exists and time is still remaining
     */
    public static boolean hasActiveTimer() {
    	return targetEndTime != null && getSecondsLeft() > 0;
    }

    /**
     * Starts a new visit session with a fixed duration.
     *
     * @param durationInSeconds visit duration in seconds
     */
    public static void startVisitSession(int durationInSeconds) {
        long currentTime = System.currentTimeMillis();
        targetEndTime = currentTime + (durationInSeconds * 1000);
        visitStarted = true;
    }

    /**
     * Returns the remaining time for the visit.
     *
     * @return number of seconds left, or 0 if no active session
     */
    public static Integer getSecondsLeft() {
        if (targetEndTime == null) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        long diff = targetEndTime - currentTime;
        return (int) (diff / 1000);
    }

    /**
     * Indicates whether a visit session is marked as started.
     *
     * @return true if visit has started
     */
    public static boolean isVisitStarted() {
        return visitStarted;
    }

    /**
     * Updates the visit started flag.
     *
     * @param started true if visit started, false otherwise
     */
    public static void setVisitStarted(boolean started) {
        visitStarted = started;
    }

    /**
     * Clears the current visit session and resets all state.
     */
    public static void clear() {
        targetEndTime = null;
        visitStarted = false;
    }
}
