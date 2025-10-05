package air.intelligence.domain;

import air.intelligence.value.Coord;
import air.intelligence.value.WarningLevel;
import lombok.*;
import nl.martijndwars.webpush.Subscription;
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
    private Subscription pushSubscription;
    private WarningLevel warningLevel;

    public void updateLastCoord(Coord coord) {
        this.lastCoord = coord;
    }

    public void subscribe(Subscription subscription) {
        this.pushSubscription = subscription;
    }

    public void updateWarningLevel(WarningLevel warningLevel) {
        this.warningLevel = warningLevel;
    }

    public boolean isNotifiable() {
        return this.pushSubscription != null;
    }
}
