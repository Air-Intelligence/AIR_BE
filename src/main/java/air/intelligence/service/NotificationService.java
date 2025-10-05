package air.intelligence.service;

import air.intelligence.domain.User;
import air.intelligence.dto.SubscriptionRequest;
import air.intelligence.value.WarningLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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

    public void sendNotification(WarningLevel warningLevel) {
        try {
            String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='128' height='128'>"
                    + "<rect width='100%' height='100%' fill='#0a74da'/>"
                    + "<text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='white' font-size='40'>Hi</text>"
                    + "</svg>";

            // option A: URL-encode raw SVG into a data URL (utf-8) — 크기가 줄어듦
            String svgEscaped = java.net.URLEncoder.encode(svg, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // 공백 처리
            String dataUrl = "data:image/svg+xml;utf8," + svgEscaped;

            Map<String, Object> payload = new HashMap<>();
            payload.put("title", warningLevel.getWarningMessage());
            payload.put("description", warningLevel.getWarningMessage());
            payload.put("icon", dataUrl);

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
