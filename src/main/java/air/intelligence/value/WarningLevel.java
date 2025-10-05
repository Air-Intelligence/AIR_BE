package air.intelligence.value;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum WarningLevel {
    SAFE("You are safe"),
    READY("Ready to run"),
    WARNING("Warning!"),
    DANGER("You are in danger."),
    RUN("RUN!");

    private final String warningMessage;

    public boolean isDanger() {
        return this != SAFE;
    }
}
