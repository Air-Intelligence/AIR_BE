package air.intelligence.domain;

import air.intelligence.value.GeoFeature;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "geo_feature")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class GeoFeatureData {

    @Id
    private String id;

    private String timestamp;
    private String type;
    private GeoFeature[] features;
}
