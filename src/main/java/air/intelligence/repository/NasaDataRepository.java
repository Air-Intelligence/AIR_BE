package air.intelligence.repository;

import air.intelligence.domain.NasaData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NasaDataRepository {

    public List<NasaData> findAll() {
        throw new UnsupportedOperationException();
    }
}
