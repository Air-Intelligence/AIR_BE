package air.intelligence.error.errorcode;

import air.intelligence.util.api.ErrorSpec;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonErrorCode implements ErrorSpec {
    INTERNAL_SERVER_ERROR("Server error - Blame the Back-end developer");

    private final String message;
}
