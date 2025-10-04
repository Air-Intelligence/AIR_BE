package air.intelligence.scheduler;

import air.intelligence.domain.User;
import air.intelligence.service.NasaDataService;
import air.intelligence.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.jose4j.lang.JoseException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        List<User> usersInDanger = allUsers;
        List<User> usersNotInDanger = List.of();

        usersInDanger.forEach((user) -> {
            try {
                user.updateInDanger(true);

                if (user.isNotifiable()) {
                    this.pushService.send(
                            new Notification(
                                    user.getPushSubscription(),
                                    "Hello, World!"
                            )
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        usersNotInDanger.forEach((user) ->
                user.updateInDanger(false));

        this.userService.putUsers(usersInDanger);
        this.userService.putUsers(usersNotInDanger);
    }
}
