package air.intelligence.service;

import air.intelligence.domain.Coord;
import air.intelligence.domain.User;
import air.intelligence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public String addUserIfNotExist() {
        String generatedId = UUID.randomUUID() + "_" + System.currentTimeMillis();

        User user = User.builder()
                .id(generatedId)
                .lastCoord(Coord.of(13.13, 15.25))
                .build();

        this.userRepository.save(user);

        log.info("user={}", user);

        return generatedId;
    }

    public void updateLastCoord(String userId, Coord coord) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(NoSuchElementException::new);

        user.updateLastCoord(coord);
    }
}
