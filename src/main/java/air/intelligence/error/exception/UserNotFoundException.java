package air.intelligence.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Getter
public class UserNotFoundException extends NoSuchElementException {
    private final String userId;

    public UserNotFoundException(String userId, Throwable cause) {
        super(cause);
        this.userId = userId;
    }
}
