package air.intelligence.repository;

import air.intelligence.domain.NasaData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface WeatherRepository extends MongoRepository<NasaData, String> {

    @Query("{ 'lat': { $gte: ?0, $lte: ?2 }, 'lon': { $gte: ?1, $lte: ?3 } }")
    List<NasaData> findByLatLonRange(double lowerLat, double lowerLon, double upperLat, double upperLon);
}
