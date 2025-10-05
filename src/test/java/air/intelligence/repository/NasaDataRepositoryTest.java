package air.intelligence.repository;

import air.intelligence.repository.dto.No2DataDto;
import air.intelligence.repository.dto.No2ResponseDto;
import air.intelligence.repository.dto.Pm25PredictionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class NasaDataRepositoryTest {

    NasaDataRepository nasaDataRepository;

    @BeforeEach
    void setUp() {
        this.nasaDataRepository = new NasaDataRepository(new RestTemplate());
    }

    @Test
    void test() {
        List<No2DataDto> data = this.nasaDataRepository.findNo2().getData();

        Double avg = data.stream()
                .map(No2DataDto::getNo2)
                .reduce(0.0, Double::sum, Double::sum)
                / data.size();

        Double stdDev = Math.sqrt(
                data.stream()
                        .map(No2DataDto::getNo2)
                        .map((num) -> num - avg)
                        .map((num) -> Math.pow(num, 2))
                        .reduce(0.0, Double::sum, Double::sum)
        );

        List<No2DataDto> normalized = data.stream()
                .map((dat) -> new No2DataDto(dat.getLat(), dat.getLon(), (dat.getNo2() - avg) / stdDev))
                .toList();

        System.out.println("------------------------");

        normalized.stream().sorted((e1, e2) -> (int) (e1.getNo2() * 10000 - e2.getNo2() * 10000))
                .forEach(System.out::println);
    }

    @Test
    void test2() {
        Pm25PredictionDto result = this.nasaDataRepository.findPrediction(37.7749, -122.4194);
        System.out.println("here");
        System.out.println(result);
        System.out.println(result.getPred_pm25());
    }
}