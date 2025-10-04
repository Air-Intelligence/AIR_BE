package air.intelligence.service;

import air.intelligence.dto.GeoResponse;

public interface WeatherService {

    GeoResponse getPolygonWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon);

    GeoResponse getPointedWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon);
}
