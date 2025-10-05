package air.intelligence.value;

public enum WarningLevel {
    SAFE,
    READY,
    WARNING,
    DANGER,
    RUN;

    public boolean isDanger() {
        return this != SAFE;
    }
}
