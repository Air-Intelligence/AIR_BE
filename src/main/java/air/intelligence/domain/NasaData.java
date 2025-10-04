package air.intelligence.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "nasa_data")
public class NasaData {

    @Id
    private String id;

    private String kind;
    private double lat;
    private double lon;
    private double value;
}
