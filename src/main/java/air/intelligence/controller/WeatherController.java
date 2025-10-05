package air.intelligence.controller;

import air.intelligence.dto.GeoResponse;
import air.intelligence.service.WeatherService;
import air.intelligence.util.api.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weathers")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping("/polygon")
    public ResponseEntity<BaseResponse<GeoResponse>> getPolygonWeatherData(@RequestParam("lower-lat") double lowerLat,
                                                                           @RequestParam("lower-lon") double lowerLon,
                                                                           @RequestParam("upper-lat") double upperLat,
                                                                           @RequestParam("upper-lon") double upperLon) {
        return ResponseEntity.ok(
                BaseResponse.of(
                        200,
                        this.weatherService.getPolygonWeatherData()
                )
        );
    }

    @GetMapping("/point")
    public ResponseEntity<BaseResponse<GeoResponse>> getPointedWeatherData(@RequestParam("lower-lat") double lowerLat,
                                                                           @RequestParam("lower-lon") double lowerLon,
                                                                           @RequestParam("upper-lat") double upperLat,
                                                                           @RequestParam("upper-lon") double upperLon) {
        return ResponseEntity.ok(
                BaseResponse.of(
                        200,
                        this.weatherService.getPointedWeatherData()
                )
        );
    }
}
