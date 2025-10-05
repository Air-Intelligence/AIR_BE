package air.intelligence.repository;

import air.intelligence.domain.NasaData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WeatherRepository extends MongoRepository<NasaData, String> {
}
