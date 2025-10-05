package air.intelligence.dto;

import air.intelligence.value.Coord;
import air.intelligence.value.WarningLevel;

public record LastCoordUpdateResponse(
        String userId,
        Coord coord,
        WarningLevel warningLevel
) {
}
