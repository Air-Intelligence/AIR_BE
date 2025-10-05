package air.intelligence.service;

import air.intelligence.domain.User;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final UserService userService;
    private final PushService pushService;
    private final ObjectMapper objectMapper;

    public void subscribe(SubscriptionRequest dto) {
        User user = this.userService.findUser(dto.getUserId());
        user.subscribe(dto.getSubscription());
        this.userService.putUser(user);
    }

    public void sendNotification() {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", "hello");
            payload.put("description", "world!");
            payload.put("icon", "/spring-svgrepo-com.svg");
            payload.put("badge", "/spring-svgrepo-com.svg");
            payload.put("image", "/spring-svgrepo-com.svg");

            String payloadJson = objectMapper.writeValueAsString(payload);

            for (Subscription subscription : this.userService.findAllUsers()
                    .stream()
                    .map(User::getPushSubscription)
                    .toList()) {
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
