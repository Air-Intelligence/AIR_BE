package air.intelligence.repository;

import air.intelligence.domain.NasaData;
import air.intelligence.repository.dto.No2ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository
@RequiredArgsConstructor
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
}
