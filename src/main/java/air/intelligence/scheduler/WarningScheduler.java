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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper om;

    @Scheduled(fixedRate = 1000 * 60 * 5)
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

        // Pre-calculate and save polygon and point geo features
        this.geoFeatureDataRepository.deleteAll();
        calculateAndSaveGeoFeatures(result);

        // Retrieve the polygon features
        GeoFeature[] polygonFeatures = geoFeatureDataRepository.findByType("polygon")
                .map(GeoFeatureData::getFeatures)
                .orElse(new GeoFeature[0]);

        Map<WarningLevel, Collection<User>> usersByWarningLevel = new HashMap<>();

        for (User user : allUsers) {
            double userLat = user.getLastCoord().getLat();
            double userLon = user.getLastCoord().getLon();

            // Check if user is inside any polygon
            WarningLevel warningLevel = null;
            for (GeoFeature feature : polygonFeatures) {
                if (isPointInPolygon(userLon, userLat, feature.geometry().coordinates())) {
                    warningLevel = (WarningLevel) feature.properties().value();
                    break;
                }
            }

            // If not in any polygon, default to SAFE
            if (warningLevel == null) {
                warningLevel = WarningLevel.SAFE;
            }

            usersByWarningLevel.computeIfAbsent(warningLevel, k -> new ArrayList<>()).add(user);
        }

        usersByWarningLevel.forEach((warningLevel, users) -> {
            final Map<String, Object> pushPayload = Map.of(
                    "title", warningLevel.getWarningMessage(),
                    "description", warningLevel.getWarningMessage()
            );
            String payloadAsString;
            try {
                payloadAsString = this.om.writeValueAsString(pushPayload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            users.forEach((user) -> {
                try {
                    if (warningLevel.isDanger() && user.isNotifiable()) {
                        this.pushService.send(
                                new Notification(
                                        user.getPushSubscription(),
                                        payloadAsString
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

    private boolean isPointInPolygon(double lon, double lat, Object coordinates) {
        if (!(coordinates instanceof double[][][])) {
            return false;
        }

        double[][][] polygonCoords = (double[][][]) coordinates;
        if (polygonCoords.length == 0 || polygonCoords[0].length == 0) {
            return false;
        }

        // Use ray casting algorithm for point-in-polygon test
        double[] ring = polygonCoords[0][0];
        boolean inside = false;
        int n = polygonCoords[0].length;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double[] pi = polygonCoords[0][i];
            double[] pj = polygonCoords[0][j];

            double xi = pi[0], yi = pi[1];
            double xj = pj[0], yj = pj[1];

            boolean intersect = ((yi > lat) != (yj > lat))
                    && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi);

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }
}
