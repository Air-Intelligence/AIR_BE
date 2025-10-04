package air.intelligence.controller;

import air.intelligence.dto.LastCoordUpdateRequest;
import air.intelligence.dto.UserCreationDto;
import air.intelligence.service.UserService;
import air.intelligence.util.api.BaseResponse;
import air.intelligence.util.http.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @PostMapping
    public ResponseEntity<BaseResponse<UserCreationDto>> generateId() {
        return new ResponseEntity<>(
                BaseResponse.of(200, this.userService.addUser()),
                HttpStatus.OK
        );
    }

    @PutMapping("/last-coord")
    public ResponseEntity<BaseResponse<Void>> putCoord(@RequestBody LastCoordUpdateRequest dto) {
        this.userService.updateLastCoord(dto);

        return ResponseEntity.ok(
                BaseResponse.of(200, null)
        );
    }
}
