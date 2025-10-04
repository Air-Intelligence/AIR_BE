package air.intelligence.util.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class ErrorResponse {
    private final String errorName;
    private final String message;
    private LocalDateTime timestamp;

    public static ErrorResponse from(ErrorSpec errorSpec) {
        return new ErrorResponse(
                errorSpec.name(),
                errorSpec.getMessage(),
                LocalDateTime.now()
        );
    }
}
