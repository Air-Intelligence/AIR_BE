package air.intelligence.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WarningConstant {
    public static final double RUN_VAL = 7.8;
    public static final double DANGER_VAL = 6.0;
    public static final double WARNING_VAL = 4.0;
    public static final double READY_VAL = 2.0;
}
