package air.intelligence.controller;

import air.intelligence.constant.UserConstant;
import air.intelligence.service.UserService;
import air.intelligence.util.api.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> generateId(
            @CookieValue(name = "UserConstant.USER_ID_COOKIE_NAME") String userId) {
        if (userId == null) {
            String newUserId = this.userService.addUserIfNotExist();

            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    HttpHeaders.SET_COOKIE,
                    ResponseCookie.from(UserConstant.USER_ID_COOKIE_NAME, newUserId)
                            .domain("localhost") // TODO
                            .httpOnly(true)
                            .secure(true)
                            .maxAge(Duration.ofDays(365))
                            .build()
                            .toString()
            );

            return new ResponseEntity<>(
                    BaseResponse.of(201, null),
                    headers,
                    HttpStatus.CREATED
            );
        }

        return new ResponseEntity<>(
                BaseResponse.of(200, null),
                HttpStatus.OK
        );
    }
}
