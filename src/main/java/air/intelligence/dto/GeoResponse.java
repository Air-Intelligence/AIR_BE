package air.intelligence.dto;

import air.intelligence.value.GeoFeature;

public record GeoResponse(
        String type,
        GeoFeature[] features
) {
}
