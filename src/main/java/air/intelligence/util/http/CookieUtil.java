package air.intelligence.util.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {

    @Value("${cookie.domain}")
    private String domain;

    public String buildCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .domain(this.domain)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(365))
                .build()
                .toString();
    }
}
