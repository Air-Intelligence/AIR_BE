package air.intelligence.repository;

import air.intelligence.domain.GeoFeatureData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface GeoFeatureDataRepository extends MongoRepository<GeoFeatureData, String> {

    @Query("{ 'type': { $eq: ?0 } }")
    Optional<GeoFeatureData> findByType(String type);
}
