package air.intelligence.error.handler;

import air.intelligence.error.errorcode.UserErrorCode;
import air.intelligence.error.exception.UserNotFoundException;
import air.intelligence.util.api.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "air.intelligence.controller")
@Slf4j
public class CommonErrorHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFoundException(UserNotFoundException e) {
        log.info("User not found: {}", e.getUserId());
        return new ResponseEntity<>(ErrorResponse.from(UserErrorCode.USER_NOT_FOUND), HttpStatus.NOT_FOUND);
    }
}
