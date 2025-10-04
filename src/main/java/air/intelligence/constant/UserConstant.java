package air.intelligence.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum UserConstant {
    USER_ID_COOKIE_NAME("client_id");

    private final String value;

    public String getString() {
        return this.value;
    }
}
