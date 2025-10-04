package air.intelligence.service;

import air.intelligence.dto.LastCoordUpdateRequest;
import air.intelligence.dto.UserCreationDto;
import air.intelligence.value.Coord;
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

    public UserCreationDto addUser() {
        String generatedId = UUID.randomUUID() + "_" + System.currentTimeMillis();

        User user = User.builder()
                .id(generatedId)
                .lastCoord(Coord.of(13.13, 15.25))
                .build();

        this.userRepository.save(user);

        log.info("user={}", user);

        return new UserCreationDto(generatedId);
    }

    public void updateLastCoord(LastCoordUpdateRequest dto) {
        User user = this.userRepository.findById(dto.getUserId())
                .orElseThrow(NoSuchElementException::new);

        user.updateLastCoord(dto.getCoord());

        this.userRepository.save(user);
    }
}
