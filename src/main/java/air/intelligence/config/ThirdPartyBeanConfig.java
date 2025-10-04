package air.intelligence.config;

import air.intelligence.etc.RestTemplateErrorHandler;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.Duration;

@Configuration
public class ThirdPartyBeanConfig {

    @Value("${web-push.vapid.public-key}")
    private String publicKey;

    @Value("${web-push.vapid.private-key}")
    private String privateKey;

    @Bean
    public PushService defaultPushService() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        return new PushService(this.publicKey, this.privateKey);
    }

    @Bean
    public RestTemplate defaultRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofMinutes(1))
                .errorHandler(new RestTemplateErrorHandler())
                .build();
    }
}
