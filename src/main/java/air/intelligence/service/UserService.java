package air.intelligence.service;

import air.intelligence.dto.LastCoordUpdateRequest;
import air.intelligence.dto.UserCreationDto;
import air.intelligence.error.exception.UserNotFoundException;
import air.intelligence.value.Coord;
import air.intelligence.domain.User;
import air.intelligence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
                .inDanger(false)
                .build();

        this.userRepository.save(user);

        log.info("user={}", user);
        return new UserCreationDto(generatedId);
    }

    public void updateLastCoord(LastCoordUpdateRequest dto) {
        User user = this.userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

        user.updateLastCoord(dto.getCoord());

        this.userRepository.save(user);

        // TODO: LastCoordUpdateResponse
    }

    public void putUser(User user) {
        this.userRepository.save(user);
    }

    public void putUsers(Iterable<User> users) {
        this.userRepository.saveAll(users);
    }

    public User findUser(String id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> findAllUsers() {
        return this.userRepository.findAll();
    }
}
