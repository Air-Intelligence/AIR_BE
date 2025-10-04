package air.intelligence.domain;

import air.intelligence.value.Coord;
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
    private boolean inDanger;

    public void updateLastCoord(Coord coord) {
        this.lastCoord = coord;
    }

    public void subscribe(Subscription subscription) {
        this.pushSubscription = subscription;
    }

    public void updateInDanger(boolean inDanger) {
        this.inDanger = inDanger;
    }

    public boolean isNotifiable() {
        return this.pushSubscription != null;
    }
}
