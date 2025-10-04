package air.intelligence.error.errorcode;

import air.intelligence.util.api.ErrorSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum UserErrorCode implements ErrorSpec {
    USER_NOT_FOUND("User not found");

    private final String message;
}
