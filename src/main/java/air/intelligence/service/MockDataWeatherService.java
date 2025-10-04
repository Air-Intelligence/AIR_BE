package air.intelligence.service;

import air.intelligence.dto.GeoResponse;
import air.intelligence.value.GeoFeature;
import air.intelligence.value.GeoFeatureType;
import air.intelligence.value.Geometry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("mock-data")
public class MockDataWeatherService implements WeatherService {

    @Override
    public GeoResponse getPolygonWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                new GeoFeature[] {
                        new GeoFeature(
                                GeoFeatureType.FEATURE.asPascalCase(),
                                new Geometry(
                                        GeoFeatureType.POLYGON.asPascalCase(),
                                        new double[][][] {
                                                {
                                                        { 126.934657, 37.5 },
                                                        { 126.973625, 36.507333 },
                                                        { 126.972766, 37.51717137 },
                                                        { 126.949077, 37.524898 },
                                                        { 126.934657, 37.5 }
                                                }
                                        }
                                ),
                                Map.of("value", 60.0)
                        )
                }
        );
    }

    @Override
    public GeoResponse getPointedWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                new GeoFeature[] {
                        new GeoFeature(
                                GeoFeatureType.FEATURE.asPascalCase(),
                                new Geometry(
                                        GeoFeatureType.POINT.asPascalCase(),
                                        new double[] { 126.842733, 37.528787 }
                                ),
                                Map.of("value", 60.0)
                        ),
                        new GeoFeature(
                                GeoFeatureType.FEATURE.asPascalCase(),
                                new Geometry(
                                        GeoFeatureType.POINT.asPascalCase(),
                                        new double[] { 126.907964, 37.533859 }
                                ),
                                Map.of("value", 45.64)
                        ),
                        new GeoFeature(
                                GeoFeatureType.FEATURE.asPascalCase(),
                                new Geometry(
                                        GeoFeatureType.POINT.asPascalCase(),
                                        new double[] { 126.907621, 37.568155 }
                                ),
                                Map.of("value", 30.0)
                        ),
                        new GeoFeature(
                                GeoFeatureType.FEATURE.asPascalCase(),
                                new Geometry(
                                        GeoFeatureType.POINT.asPascalCase(),
                                        new double[] { 126.852346, 37.567882 }
                                ),
                                Map.of("value", 50.0)
                        )

                }
        );
    }
}
