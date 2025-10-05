package air.intelligence.controller;

import air.intelligence.dto.SubscriptionRequest;
import air.intelligence.service.NotificationService;
import air.intelligence.util.api.BaseResponse;
import air.intelligence.value.WarningLevel;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Subscription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;


    @PostMapping("/subscribe")
    public ResponseEntity<BaseResponse<String>> subscribe(@RequestBody SubscriptionRequest dto) {
        notificationService.subscribe(dto);
        return ResponseEntity.ok(BaseResponse.of(200, "Subscribed successfully"));
    }

    @GetMapping("/sample")
    public void sample(@RequestParam(value = "warning-level", defaultValue = "SAFE") WarningLevel warningLevel) {
        this.notificationService.sendNotification(warningLevel);
    }
}
