package air.intelligence.value;

import java.util.Arrays;

public enum GeoFeatureType {
    FEATURE_COLLECTION,
    FEATURE,
    POINT,
    LINE_STRING,
    POLYGON,
    MULTIPOINT,
    MULTI_LINE_STRING,
    MULTI_POLYGON;

    public String asPascalCase() {
        return Arrays.stream(name().split("_"))
                .map(this::lowerCaseIgnoringFirstLetter)
                .reduce("", String::concat, String::concat);
    }

    private String lowerCaseIgnoringFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
