package air.intelligence.controller;

import air.intelligence.constant.UserConstant;
import air.intelligence.util.api.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @GetMapping("/id")
    public ResponseEntity<BaseResponse<Void>> generateId() {
        String userId = UUID.randomUUID().toString() + "_" + System.currentTimeMillis();

        HttpHeaders headers = new HttpHeaders();
        headers.add(
                HttpHeaders.SET_COOKIE,
                ResponseCookie.from(UserConstant.USER_ID_COOKIE_NAME.getString(), userId)
                        .domain("localhost") // TODO
                        .httpOnly(true)
                        .secure(true)
                        .maxAge(Duration.ofDays(365))
                        .build()
                        .toString()
        );

        return new ResponseEntity<>(
                BaseResponse.of(200, null),
                headers,
                HttpStatus.OK
        );
    }
}
