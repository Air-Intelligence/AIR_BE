package air.intelligence.common.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class BaseResponse<B> {
    private final int statusCode;
    private final B content;
    private final LocalDateTime timestamp;

    public static <B> BaseResponse<B> of(int statusCode, B content) {
        return new BaseResponse<>(statusCode, content, LocalDateTime.now());
    }

    public static <B> BaseResponse<B> of(HttpStatus httpStatus, B content) {
        return of(httpStatus.value(), content);
    }
}
