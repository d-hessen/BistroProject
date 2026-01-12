package handlers;

public class VisitSessionManager {

    private static Long targetEndTime;
    private static boolean visitStarted;

    public static boolean hasActiveTimer() {
    	return targetEndTime != null && getSecondsLeft() > 0;
    }

    public static void startVisitSession(int durationInSeconds) {
        long currentTime = System.currentTimeMillis();
        targetEndTime = currentTime + (durationInSeconds * 1000);
        visitStarted = true;
    }

    public static Integer getSecondsLeft() {
        if (targetEndTime == null) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        long diff = targetEndTime - currentTime;
        return (int) (diff / 1000);
    }

    public static boolean isVisitStarted() {
        return visitStarted;
    }

    public static void setVisitStarted(boolean started) {
        visitStarted = started;
    }

    public static void clear() {
        targetEndTime = null;
        visitStarted = false;
    }
}
