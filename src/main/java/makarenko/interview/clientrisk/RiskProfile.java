package makarenko.interview.clientrisk;

public enum RiskProfile {
    LOW,
    NORMAL,
    HIGH;

    public static final RiskProfile[] VALUES = values();
    public static final int LENGTH = values().length;

    public static RiskProfile of(int ordinal) {
        return VALUES[ordinal];
    }
}
