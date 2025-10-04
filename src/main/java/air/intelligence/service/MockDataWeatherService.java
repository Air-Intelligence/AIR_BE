package air.intelligence.service;

import air.intelligence.dto.GeoResponse;
import air.intelligence.value.GeoFeature;
import air.intelligence.value.GeoFeatureType;
import air.intelligence.value.GeoProperties;
import air.intelligence.value.Geometry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
@Profile("mock-data")
public class MockDataWeatherService implements WeatherService {

    @Override
    public GeoResponse getPolygonWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                Arrays.stream(
                                new GeoFeature[]{
                                        new GeoFeature(
                                                GeoFeatureType.FEATURE.asPascalCase(),
                                                new Geometry(
                                                        GeoFeatureType.POLYGON.asPascalCase(),
                                                        new double[][][]{
                                                                {
                                                                        {126.934657, 37.5},
                                                                        {126.973625, 36.507333},
                                                                        {126.972766, 37.51717137},
                                                                        {126.949077, 37.524898},
                                                                        {126.934657, 37.5}
                                                                }
                                                        }
                                                ),
                                                new GeoProperties(60.0)
                                        ),
                                        new GeoFeature(
                                                GeoFeatureType.FEATURE.asPascalCase(),
                                                new Geometry(
                                                        GeoFeatureType.POLYGON.asPascalCase(),
                                                        new double[][][]{
                                                                {
                                                                        {126.832834, 37.430823},
                                                                        {126.956430, 37, 405192},
                                                                        {127.104059, 37.480425},
                                                                        {127.024061, 37.565985},
                                                                        {126.953790, 37.568794},
                                                                        {126.896510, 37.562709},
                                                                        {126.832834, 37.430823}
                                                                }
                                                        }
                                                ),
                                                new GeoProperties(30.0)
                                        )
                                }
                        )
                        .sorted((n1, n2) ->
                                (int) (n1.properties().value() * 100 - n2.properties().value() * 100))
                        .toArray(GeoFeature[]::new)
        );
    }

    @Override
    public GeoResponse getPointedWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                Arrays.stream(
                                new GeoFeature[]{
                                        new GeoFeature(
                                                GeoFeatureType.FEATURE.asPascalCase(),
                                                new Geometry(
                                                        GeoFeatureType.POINT.asPascalCase(),
                                                        new double[]{126.842733, 37.528787}
                                                ),
                                                new GeoProperties(60.0)
                                        ),
                                        new GeoFeature(
                                                GeoFeatureType.FEATURE.asPascalCase(),
                                                new Geometry(
                                                        GeoFeatureType.POINT.asPascalCase(),
                                                        new double[]{126.907964, 37.533859}
                                                ),
                                                new GeoProperties(45.64)
                                        ),
                                        new GeoFeature(
                                                GeoFeatureType.FEATURE.asPascalCase(),
                                                new Geometry(
                                                        GeoFeatureType.POINT.asPascalCase(),
                                                        new double[]{126.907621, 37.568155}
                                                ),
                                                new GeoProperties(30.0)
                                        ),
                                        new GeoFeature(
                                                GeoFeatureType.FEATURE.asPascalCase(),
                                                new Geometry(
                                                        GeoFeatureType.POINT.asPascalCase(),
                                                        new double[]{126.852346, 37.567882}
                                                ),
                                                new GeoProperties(50.0)
                                        )

                                }
                        )
                        .sorted((n1, n2) ->
                                (int) (n1.properties().value() * 100 - n2.properties().value() * 100))
                        .toArray(GeoFeature[]::new)
        );
    }
}
