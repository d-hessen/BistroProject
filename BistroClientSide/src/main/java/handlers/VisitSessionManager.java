package handlers;

import javafx.animation.Timeline;

public class VisitSessionManager {

    private static Integer secondsLeft;
    private static Timeline countdown;
    private static boolean visitStarted;

    public static boolean hasActiveTimer() {
        return secondsLeft != null && secondsLeft > 0;
    }

    public static void startTimer(int seconds, Timeline timeline) {
        secondsLeft = seconds;
        countdown = timeline;
    }

    public static Integer getSecondsLeft() {
        return secondsLeft;
    }

    public static void setSecondsLeft(int seconds) {
        secondsLeft = seconds;
    }

    public static Timeline getCountdown() {
        return countdown;
    }

    public static boolean isVisitStarted() {
        return visitStarted;
    }

    public static void setVisitStarted(boolean started) {
        visitStarted = started;
    }

    public static void clear() {
        if (countdown != null) {
            countdown.stop();
        }
        secondsLeft = null;
        countdown = null;
        visitStarted = false;
    }
}
