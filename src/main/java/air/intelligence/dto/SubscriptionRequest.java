package air.intelligence.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.martijndwars.webpush.Subscription;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class SubscriptionRequest {

    @NotNull
    private String userId;

    private Subscription subscription;
}
