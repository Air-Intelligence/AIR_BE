package air.intelligence.domain;

import air.intelligence.value.Coord;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class User {

    @Id
    private String id;

    private Coord lastCoord;

    public void updateLastCoord(Coord coord) {
        this.lastCoord = coord;
    }
}
