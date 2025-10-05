package air.intelligence.config;

import air.intelligence.repository.WeatherRepository;
import air.intelligence.service.DefaultWeatherService;
import air.intelligence.service.WeatherService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    @ConditionalOnMissingBean(WeatherService.class)
    public WeatherService defaultWeatherService(WeatherRepository weatherRepository) {
        return new DefaultWeatherService(weatherRepository);
    }
}
