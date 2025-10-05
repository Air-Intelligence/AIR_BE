package air.intelligence.scheduler;

import air.intelligence.config.WarningConstant;
import air.intelligence.domain.NasaData;
import air.intelligence.domain.User;
import air.intelligence.repository.NasaDataRepository;
import air.intelligence.repository.WeatherRepository;
import air.intelligence.repository.dto.No2DataDto;
import air.intelligence.repository.dto.No2ResponseDto;
import air.intelligence.service.UserService;
import air.intelligence.value.WarningLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarningScheduler {
    private final UserService userService;
    private final PushService pushService;
    private final NasaDataRepository nasaDataRepository;
    private final WeatherRepository weatherRepository;

    @Scheduled(cron = "0 0 */1 * * ?")
    public void task() {
        List<User> allUsers = this.userService.findAllUsers();

        List<No2DataDto> result = this.nasaDataRepository.findNo2().getData();

        String timestamp = LocalDateTime.now().toString();
        this.weatherRepository.deleteAll();
        this.weatherRepository.saveAll(result.stream()
                .map((r) -> NasaData.builder()
                        .timestamp(timestamp)
                        .kind("no2")
                        .lat(r.getLat())
                        .lon(r.getLon())
                        .value(r.getNo2())
                        .build())
                .toList());

        TreeMap<Double, No2DataDto> byLat = new TreeMap<>();
        TreeMap<Double, No2DataDto> byLon = new TreeMap<>();
        for (No2DataDto d : result) {
            byLat.put(d.getLat(), d);
            byLon.put(d.getLon(), d);
        }

        Map<WarningLevel, Collection<User>> usersByWarningLevel = new HashMap<>();

        for (User user : allUsers) {
            double userLat = user.getLastCoord().getLat();
            double userLon = user.getLastCoord().getLon();

            // Find closest NO2 data by lat
            Map.Entry<Double, No2DataDto> lowerLat = byLat.floorEntry(userLat);
            Map.Entry<Double, No2DataDto> higherLat = byLat.ceilingEntry(userLat);

            // Find closest NO2 data by lon
            Map.Entry<Double, No2DataDto> lowerLon = byLon.floorEntry(userLon);
            Map.Entry<Double, No2DataDto> higherLon = byLon.ceilingEntry(userLon);

            // Get the closest NO2 data point
            No2DataDto closestNo2Data = null;
            double minDistance = Double.MAX_VALUE;

            for (Map.Entry<Double, No2DataDto> latEntry : Arrays.asList(lowerLat, higherLat)) {
                for (Map.Entry<Double, No2DataDto> lonEntry : Arrays.asList(lowerLon, higherLon)) {
                    if (latEntry != null && lonEntry != null) {
                        for (No2DataDto data : result) {
                            if (data.getLat() == latEntry.getValue().getLat() ||
                                data.getLon() == lonEntry.getValue().getLon()) {
                                double distance = Math.sqrt(
                                    Math.pow(data.getLat() - userLat, 2) +
                                    Math.pow(data.getLon() - userLon, 2)
                                );
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    closestNo2Data = data;
                                }
                            }
                        }
                    }
                }
            }

            // If no data found, use first available data as fallback
            if (closestNo2Data == null && !result.isEmpty()) {
                closestNo2Data = result.get(0);
            }

            // Determine warning level based on NO2 value
            WarningLevel warningLevel;
            if (closestNo2Data != null) {
                double no2Value = closestNo2Data.getNo2();

                // Example logic (replace with your thresholds):
                if (no2Value > WarningConstant.RUN_VAL) {
                    warningLevel = WarningLevel.RUN;
                } else if (no2Value > WarningConstant.DANGER_VAL) {
                    warningLevel = WarningLevel.DANGER;
                } else if (no2Value > WarningConstant.WARNING_VAL) {
                    warningLevel = WarningLevel.WARNING;
                } else if (no2Value > WarningConstant.READY_VAL) {
                    warningLevel = WarningLevel.READY;
                } else {
                    warningLevel = WarningLevel.SAFE;
                }
            } else {
                warningLevel = WarningLevel.SAFE; // Default if no data
            }

            usersByWarningLevel.computeIfAbsent(warningLevel, k -> new ArrayList<>()).add(user);
        }

        usersByWarningLevel.forEach((warningLevel, users) -> {
            users.forEach((user) -> {
                try {
                    if (warningLevel.isDanger() && user.isNotifiable()) {
                        this.pushService.send(
                                new Notification(
                                        user.getPushSubscription(),
                                        "Hello, World!"
                                )
                        );
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    user.updateWarningLevel(warningLevel);
                }
            });
        });

        this.userService.putUsers(usersByWarningLevel.values().stream().flatMap(Collection::stream).toList());
    }
}
