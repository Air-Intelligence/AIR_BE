package air.intelligence.value;

public record GeoFeature (
        String type,
        Geometry geometry,
        GeoProperties properties
) {
}
