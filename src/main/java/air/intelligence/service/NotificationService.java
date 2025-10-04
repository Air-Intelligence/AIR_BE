package air.intelligence.service;

import air.intelligence.dto.SubscriptionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final PushService pushService;
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void subscribe(SubscriptionRequest dto) {
        if (this.subscriptions.containsKey(dto.getUserId())) {
            return;
        }
        this.subscriptions.put(dto.getUserId(), dto.getSubscription());
        log.info("New subscription added. Total subscriptions: {}", subscriptions.size());
    }

    public void sendNotification() {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("title", "hello");
            payload.put("description", "world!");

            String payloadJson = objectMapper.writeValueAsString(payload);

            for (Subscription subscription : subscriptions.values()) {
                try {
                    Notification notification = new Notification(subscription, payloadJson);
                    pushService.send(notification);
                    log.info("Push notification sent successfully");
                } catch (Exception e) {
                    log.error("Failed to send push notification to subscription", e);
                }
            }
        } catch (Exception e) {
            log.error("Error creating notification payload", e);
        }
    }
}
