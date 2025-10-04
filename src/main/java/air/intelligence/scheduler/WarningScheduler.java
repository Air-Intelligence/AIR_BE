package air.intelligence.scheduler;

import air.intelligence.domain.User;
import air.intelligence.service.NasaDataService;
import air.intelligence.service.UserService;
import air.intelligence.value.WarningLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarningScheduler {
    private final UserService userService;
    private final PushService pushService;
    private final NasaDataService nasaDataService;

//    @Scheduled(fixedRate = 10 * 60 * 1000)
    @Scheduled(fixedRate = 1000)
    public void task() {
        List<User> allUsers = this.userService.findAllUsers();

        // TODO: find users in not good air condition
        Map<WarningLevel, Collection<User>> usersByWarningLevel = allUsers.stream().collect(
                Collectors.toMap((key) -> WarningLevel.WARNING,
                        Arrays::asList,
                        (col1, col2) -> Stream.concat(col1.stream(), col2.stream()).toList())
        );

        usersByWarningLevel.forEach((warningLevel, users) -> {
            users.forEach((user) -> {
                try {
                    if (warningLevel.isDanger() && user.isNotifiable()) {
                        this.pushService.send(
                                new Notification(
                                        user.getPushSubscription(),
                                        "Hello, World!"
                                )
                        );
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    user.updateWarningLevel(warningLevel);
                }
            });
        });

        this.userService.putUsers(usersByWarningLevel.values().stream().flatMap(Collection::stream).toList());
    }
}
