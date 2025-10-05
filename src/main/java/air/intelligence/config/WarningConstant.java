package air.intelligence.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WarningConstant {
    public static final double RUN_VAL = 3.5;
    public static final double DANGER_VAL = 2.50;
    public static final double WARNING_VAL = 1.5;
    public static final double READY_VAL = 0.5;
}
