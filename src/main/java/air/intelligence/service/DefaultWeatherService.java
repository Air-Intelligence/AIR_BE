package air.intelligence.service;

import air.intelligence.dto.GeoResponse;

public class DefaultWeatherService implements WeatherService {

    @Override
    public GeoResponse getPolygonWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeoResponse getPointedWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        throw new UnsupportedOperationException();
    }
}
