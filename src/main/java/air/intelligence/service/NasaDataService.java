package air.intelligence.service;

import air.intelligence.repository.NasaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NasaDataService {
    private final NasaDataRepository nasaDataRepository;

//    @EventListener(ApplicationReadyEvent.class)
//    public void init() {
//        List<NasaData> mostRecentDataPerKind = this.nasaDataRepository.findMostRecentDataPerKind();
//        mostRecentDataPerKind.forEach(System.out::println);
//    }

//    public Map<String, NasaData> findMostRecentNasaData() {
//
//    }
}
