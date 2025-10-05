package air.intelligence.service;

import air.intelligence.dto.GeoResponse;

public interface WeatherService {

    GeoResponse getPolygonWeatherData();

    GeoResponse getPointedWeatherData();
}
