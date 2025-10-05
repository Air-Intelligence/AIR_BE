package air.intelligence.service;

import air.intelligence.domain.GeoFeatureData;
import air.intelligence.dto.GeoResponse;
import air.intelligence.repository.GeoFeatureDataRepository;
import air.intelligence.value.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultWeatherService implements WeatherService {
    private final GeoFeatureDataRepository geoFeatureDataRepository;

    @Override
    public GeoResponse getPolygonWeatherData() {
        GeoFeatureData geoFeatureData = geoFeatureDataRepository.findByType("polygon")
                .orElse(null);

        if (geoFeatureData == null || geoFeatureData.getFeatures() == null) {
            return new GeoResponse(
                    GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                    new GeoFeature[0]
            );
        }

        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                geoFeatureData.getFeatures()
        );
    }

    @Override
    public GeoResponse getPointedWeatherData() {
        GeoFeatureData geoFeatureData = geoFeatureDataRepository.findByType("point")
                .orElseThrow(RuntimeException::new);

        if (geoFeatureData == null || geoFeatureData.getFeatures() == null) {
            return new GeoResponse(
                    GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                    new GeoFeature[0]
            );
        }

        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                geoFeatureData.getFeatures()
        );
    }
}
