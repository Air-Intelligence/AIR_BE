package air.intelligence.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "nasa_data")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class NasaData {

    @Id
    private String id;

    private String timestamp;
    private String kind;
    private double lat;
    private double lon;
    private double value;
}
