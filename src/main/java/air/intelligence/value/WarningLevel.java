package air.intelligence.value;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum WarningLevel {
    SAFE("GREEN, Safe, GOOD"),
    READY("YELLOW, Mask Ready, COUGH"),
    WARNING("ORANGE, Warning, MASK"),
    DANGER("RED, Danger, HOME"),
    RUN("BLACK, Run, !!!RUN!!!");

    private final String warningMessage;

    public boolean isDanger() {
        return this != SAFE;
    }
}
