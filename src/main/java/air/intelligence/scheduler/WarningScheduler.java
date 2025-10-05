package air.intelligence.scheduler;

import air.intelligence.config.WarningConstant;
import air.intelligence.domain.GeoFeatureData;
import air.intelligence.domain.NasaData;
import air.intelligence.domain.User;
import air.intelligence.repository.GeoFeatureDataRepository;
import air.intelligence.repository.NasaDataRepository;
import air.intelligence.repository.WeatherRepository;
import air.intelligence.repository.dto.No2DataDto;
import air.intelligence.service.UserService;
import air.intelligence.value.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarningScheduler {
    private final UserService userService;
    private final PushService pushService;
    private final NasaDataRepository nasaDataRepository;
    private final WeatherRepository weatherRepository;
    private final GeoFeatureDataRepository geoFeatureDataRepository;

    @Scheduled(cron = "0 */5 * * * ?")
    public void task() {
        log.info("Scheduled task");
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
                                        "\uD83D\uDD50 "
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

        // Pre-calculate and save polygon and point geo features
        calculateAndSaveGeoFeatures(result);
    }

    private void calculateAndSaveGeoFeatures(List<No2DataDto> no2Data) {
        String timestamp = LocalDateTime.now().toString();
        List<NasaData> nasaDataList = no2Data.stream()
                .map(r -> NasaData.builder()
                        .timestamp(timestamp)
                        .kind("no2")
                        .lat(r.getLat())
                        .lon(r.getLon())
                        .value(r.getNo2())
                        .build())
                .toList();

        // Calculate and save polygon features
        GeoFeature[] polygonFeatures = calculatePolygonFeatures(nasaDataList);
        geoFeatureDataRepository.save(GeoFeatureData.builder()
                .timestamp(timestamp)
                .type("polygon")
                .features(polygonFeatures)
                .build());

        // Calculate and save point features
        GeoFeature[] pointFeatures = calculatePointFeatures(nasaDataList);
        geoFeatureDataRepository.save(GeoFeatureData.builder()
                .timestamp(timestamp)
                .type("point")
                .features(pointFeatures)
                .build());
    }

    private GeoFeature[] calculatePolygonFeatures(List<NasaData> nasaData) {
        Map<WarningLevel, Collection<NasaData>> nasaDataPerWarningLevel = new HashMap<>();
        for (NasaData dat : nasaData) {
            double value = dat.getValue();
            WarningLevel level;
            if (value > WarningConstant.RUN_VAL) {
                level = WarningLevel.RUN;
            } else if (value > WarningConstant.DANGER_VAL) {
                level = WarningLevel.DANGER;
            } else if (value > WarningConstant.WARNING_VAL) {
                level = WarningLevel.WARNING;
            } else if (value > WarningConstant.READY_VAL) {
                level = WarningLevel.READY;
            } else {
                level = WarningLevel.SAFE;
            }
            nasaDataPerWarningLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(dat);
        }

        Map<WarningLevel, Collection<NasaData>> filteredNasaData = nasaDataPerWarningLevel.entrySet()
                .stream()
                .filter(e -> e.getValue().size() >= 3)
                .filter(e -> e.getKey() != WarningLevel.SAFE)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filteredNasaData.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
                .map(this::createPolygonGeoFeature)
                .toArray(GeoFeature[]::new);
    }

    private GeoFeature createPolygonGeoFeature(Map.Entry<WarningLevel, Collection<NasaData>> entry) {
        Collection<NasaData> dataPoints = entry.getValue();
        List<double[]> points = dataPoints.stream()
                .map(data -> new double[]{data.getLon(), data.getLat()})
                .toList();

        List<double[]> hull = computeConvexHull(points);
        hull.add(hull.get(0));

        return new GeoFeature(
                GeoFeatureType.FEATURE.asPascalCase(),
                new Geometry(
                        GeoFeatureType.POLYGON.asPascalCase(),
                        new double[][][]{hull.toArray(new double[0][])}
                ),
                new GeoProperties(entry.getKey())
        );
    }

    private List<double[]> computeConvexHull(List<double[]> points) {
        if (points.size() < 3) {
            return new ArrayList<>(points);
        }

        List<double[]> sorted = new ArrayList<>(points);
        sorted.sort((a, b) -> {
            int cmp = Double.compare(a[0], b[0]);
            return cmp != 0 ? cmp : Double.compare(a[1], b[1]);
        });

        List<double[]> lower = new ArrayList<>();
        for (double[] p : sorted) {
            while (lower.size() >= 2 && crossProduct(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p) <= 0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(p);
        }

        List<double[]> upper = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            double[] p = sorted.get(i);
            while (upper.size() >= 2 && crossProduct(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p) <= 0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(p);
        }

        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);
        lower.addAll(upper);
        return lower;
    }

    private double crossProduct(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    private GeoFeature[] calculatePointFeatures(List<NasaData> nasaData) {
        if (nasaData.isEmpty()) {
            return new GeoFeature[0];
        }

        final int latLonRange = 1;

        double latMax = nasaData.stream().mapToDouble(NasaData::getLat).max().orElse(0);
        double latMin = nasaData.stream().mapToDouble(NasaData::getLat).min().orElse(0);
        double lonMax = nasaData.stream().mapToDouble(NasaData::getLon).max().orElse(0);
        double lonMin = nasaData.stream().mapToDouble(NasaData::getLon).min().orElse(0);

        double gridLatMin = Math.floor(latMin / latLonRange) * latLonRange;
        double gridLatMax = Math.ceil(latMax / latLonRange) * latLonRange;
        double gridLonMin = Math.floor(lonMin / latLonRange) * latLonRange;
        double gridLonMax = Math.ceil(lonMax / latLonRange) * latLonRange;

        int latBoxes = (int) ((gridLatMax - gridLatMin) / latLonRange);
        int lonBoxes = (int) ((gridLonMax - gridLonMin) / latLonRange);

        if (latBoxes == 0 || lonBoxes == 0) {
            double avgValue = nasaData.stream().mapToDouble(NasaData::getValue).average().orElse(0.0);
            double centerLat = (latMax + latMin) / 2;
            double centerLon = (lonMax + lonMin) / 2;

            return new GeoFeature[]{
                    new GeoFeature(
                            GeoFeatureType.FEATURE.asPascalCase(),
                            new Geometry(
                                    GeoFeatureType.POINT.asPascalCase(),
                                    new double[]{centerLon, centerLat}
                            ),
                            new GeoProperties(avgValue)
                    )
            };
        }

        Map<String, List<Double>> scoreByBoxKey = new HashMap<>();
        for (NasaData ns : nasaData) {
            int latIdx = (int) Math.floor((ns.getLat() - gridLatMin) / latLonRange);
            int lonIdx = (int) Math.floor((ns.getLon() - gridLonMin) / latLonRange);
            String boxKey = latIdx + "," + lonIdx;
            scoreByBoxKey.computeIfAbsent(boxKey, k -> new ArrayList<>()).add(ns.getValue());
        }

        List<GeoFeature> features = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : scoreByBoxKey.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                String[] indices = entry.getKey().split(",");
                int latIdx = Integer.parseInt(indices[0]);
                int lonIdx = Integer.parseInt(indices[1]);

                double avgValue = entry.getValue().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                double centerLat = gridLatMin + (latIdx + 0.5) * latLonRange;
                double centerLon = gridLonMin + (lonIdx + 0.5) * latLonRange;

                features.add(new GeoFeature(
                        GeoFeatureType.FEATURE.asPascalCase(),
                        new Geometry(
                                GeoFeatureType.POINT.asPascalCase(),
                                new double[]{centerLon, centerLat}
                        ),
                        new GeoProperties(avgValue)
                ));
            }
        }

        return features.stream()
                .sorted((n1, n2) -> (int) ((double) n1.properties().value() * 100 - (double) n2.properties().value() * 100))
                .toArray(GeoFeature[]::new);
    }
}
