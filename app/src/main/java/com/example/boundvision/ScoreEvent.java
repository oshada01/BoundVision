package com.example.boundvision;

/**
 * Class representing a cricket score event like runs, wickets, or extras
 */
public class ScoreEvent {
    // Event types
    public static final int TYPE_RUN = 0;
    public static final int TYPE_WICKET = 1;
    public static final int TYPE_EXTRA = 2;

    private int type;       // Type of event (run, wicket, extra)
    private int value;      // Value of the event (number of runs or type of wicket/extra)
    private String overs;   // Over when the event occurred (e.g., "4.2")
    private String timestamp; // Time when the event occurred
    private boolean autoDetected; // Whether the event was detected automatically by sensors

    public ScoreEvent(int type, int value, String overs, String timestamp, boolean autoDetected) {
        this.type = type;
        this.value = value;
        this.overs = overs;
        this.timestamp = timestamp;
        this.autoDetected = autoDetected;
    }

    // Getters
    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public String getOvers() {
        return overs;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isAutoDetected() {
        return autoDetected;
    }
}