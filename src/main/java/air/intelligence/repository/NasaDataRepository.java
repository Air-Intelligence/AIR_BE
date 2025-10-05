package air.intelligence.repository;

import air.intelligence.domain.NasaData;
import air.intelligence.repository.dto.No2ResponseDto;
import air.intelligence.repository.dto.Pm25PredictionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NasaDataRepository {
    private final RestTemplate restTemplate;

    public List<NasaData> findAll() {
        throw new UnsupportedOperationException();
    }

    public No2ResponseDto findNo2() {
        RequestEntity<Void> requestEntity = RequestEntity.get("https://fastapi.bestbreathe.us/api/latest")
                .build();

        ResponseEntity<No2ResponseDto> response = this.restTemplate.exchange(requestEntity, No2ResponseDto.class);

        System.out.println(response);

        return response.getBody();
    }

    public Pm25PredictionDto findPrediction(double lat, double lon) {
        LocalDateTime when = LocalDateTime.now().plusHours(2);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("{"
                            + "\"lat\": %f,"
                            + "\"lon\": %f,"
                            + "\"when\": \"%s\""
                        + "}", lat, lon,
                String.format("%s-%s-%sT%s:%s:%s",
                        when.getYear(),
                        when.getMonthValue() < 10 ? "0" + when.getMonthValue() : String.valueOf(when.getMonthValue()),
                        when.getDayOfMonth() < 10 ? "0" + when.getDayOfMonth() : String.valueOf(when.getDayOfMonth()),
                        when.getHour() < 10 ? "0" + when.getHour() : String.valueOf(when.getHour()),
                        when.getMinute() < 10 ? "0" + when.getMinute() : String.valueOf(when.getMinute()),
                        when.getSecond() < 10 ? "0" + when.getSecond() : String.valueOf(when.getSecond())));

        RequestEntity<String> requestEntity = RequestEntity.post("https://fastapi.bestbreathe.us/api/predict/pm25")
                .headers(requestHeaders)
                .body(requestBody);

        log.info("request:\n{}", requestBody);

        ResponseEntity<Pm25PredictionDto> response = this.restTemplate.exchange(requestEntity, Pm25PredictionDto.class);

        log.info("response={}", response);

        return response.getBody();
    }
}
