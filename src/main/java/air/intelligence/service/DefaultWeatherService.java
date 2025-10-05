package air.intelligence.service;

import air.intelligence.config.WarningConstant;
import air.intelligence.domain.NasaData;
import air.intelligence.dto.GeoResponse;
import air.intelligence.repository.WeatherRepository;
import air.intelligence.value.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultWeatherService implements WeatherService {
    private final WeatherRepository weatherRepository;

    @Override
    public GeoResponse getPolygonWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        List<NasaData> nasaData = this.weatherRepository.findByLatLonRange(lowerLat, lowerLon, upperLat, upperLon);

        Map<WarningLevel, Collection<NasaData>> nasaDataPerWarningLevel = new HashMap<>();
        for (NasaData dat : nasaData) {
            double value = dat.getValue();
            if (value > WarningConstant.RUN_VAL) {
                putIntoMap(WarningLevel.RUN, dat, nasaDataPerWarningLevel);
            } else if (value > WarningConstant.DANGER_VAL) {
                putIntoMap(WarningLevel.DANGER, dat, nasaDataPerWarningLevel);
            } else if (value > WarningConstant.WARNING_VAL) {
                putIntoMap(WarningLevel.WARNING, dat, nasaDataPerWarningLevel);
            } else if (value > WarningConstant.READY_VAL) {
                putIntoMap(WarningLevel.READY, dat, nasaDataPerWarningLevel);
            } else {
                putIntoMap(WarningLevel.SAFE, dat, nasaDataPerWarningLevel);
            }
        }

        Map<WarningLevel, Collection<NasaData>> filteredNasaData = new HashMap<>();
        for (WarningLevel key : nasaDataPerWarningLevel.keySet()
                .stream()
                .filter((key) -> nasaDataPerWarningLevel.get(key).size() >= 3)
                .filter((key) -> key != WarningLevel.SAFE)
                .toList()) {
            filteredNasaData.put(key, nasaDataPerWarningLevel.get(key));
        }

        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                filteredNasaData.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
                        .map(this::createPolygonGeoFeature)
                        .toArray(GeoFeature[]::new)
        );
    }

    private GeoFeature createPolygonGeoFeature(Map.Entry<WarningLevel, Collection<NasaData>> nasaData) {
        Collection<NasaData> dataPoints = nasaData.getValue();

        // Find convex hull points for the polygon (counter-clockwise order)
        List<double[]> points = dataPoints.stream()
                .map(data -> new double[]{data.getLon(), data.getLat()})
                .toList();

        List<double[]> hull = computeConvexHull(points);

        // Close the polygon by adding the first point at the end
        hull.add(hull.get(0));

        return new GeoFeature(
                GeoFeatureType.FEATURE.asPascalCase(),
                new Geometry(
                        GeoFeatureType.POLYGON.asPascalCase(),
                        new double[][][]{
                                hull.toArray(new double[0][])
                        }
                ),
                new GeoProperties(nasaData.getKey())
        );
    }

    private List<double[]> computeConvexHull(List<double[]> points) {
        if (points.size() < 3) {
            return new ArrayList<>(points);
        }

        // Sort points by x-coordinate (lon), then by y-coordinate (lat)
        List<double[]> sorted = new ArrayList<>(points);
        sorted.sort((a, b) -> {
            int cmp = Double.compare(a[0], b[0]);
            return cmp != 0 ? cmp : Double.compare(a[1], b[1]);
        });

        // Build lower hull
        List<double[]> lower = new ArrayList<>();
        for (double[] p : sorted) {
            while (lower.size() >= 2 && crossProduct(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p) <= 0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(p);
        }

        // Build upper hull
        List<double[]> upper = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            double[] p = sorted.get(i);
            while (upper.size() >= 2 && crossProduct(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p) <= 0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(p);
        }

        // Remove last point of each half because it's repeated
        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);

        // Concatenate lower and upper hull
        lower.addAll(upper);
        return lower;
    }

    private double crossProduct(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    private void putIntoMap(WarningLevel key, NasaData val, Map<WarningLevel, Collection<NasaData>> map) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(val);
    }

    @Override
    public GeoResponse getPointedWeatherData(double lowerLat, double lowerLon, double upperLat, double upperLon) {
        List<NasaData> nasaData = this.weatherRepository.findByLatLonRange(lowerLat, lowerLon, upperLat, upperLon);

        if (nasaData.isEmpty()) {
            return new GeoResponse(
                    GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                    new GeoFeature[0]
            );
        }

        final int latLonRange = 1;

        double latMax = nasaData.stream().map(NasaData::getLat).max(Comparator.naturalOrder()).get();
        double latMin = nasaData.stream().map(NasaData::getLat).min(Comparator.naturalOrder()).get();
        double lonMax = nasaData.stream().map(NasaData::getLon).max(Comparator.naturalOrder()).get();
        double lonMin = nasaData.stream().map(NasaData::getLon).min(Comparator.naturalOrder()).get();

        // Align to grid boundaries
        double gridLatMin = Math.floor(latMin / latLonRange) * latLonRange;
        double gridLatMax = Math.ceil(latMax / latLonRange) * latLonRange;
        double gridLonMin = Math.floor(lonMin / latLonRange) * latLonRange;
        double gridLonMax = Math.ceil(lonMax / latLonRange) * latLonRange;

        int latBoxes = (int) ((gridLatMax - gridLatMin) / latLonRange);
        int lonBoxes = (int) ((gridLonMax - gridLonMin) / latLonRange);

        // Handle case where data range is smaller than latLonRange
        if (latBoxes == 0 || lonBoxes == 0) {
            // If range is too small, just return single point with average of all values
            double avgValue = nasaData.stream()
                    .mapToDouble(NasaData::getValue)
                    .average()
                    .orElse(0.0);

            double centerLat = (latMax + latMin) / 2;
            double centerLon = (lonMax + lonMin) / 2;

            return new GeoResponse(
                    GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                    new GeoFeature[]{
                            new GeoFeature(
                                    GeoFeatureType.FEATURE.asPascalCase(),
                                    new Geometry(
                                            GeoFeatureType.POINT.asPascalCase(),
                                            new double[]{centerLon, centerLat}
                                    ),
                                    new GeoProperties(avgValue)
                            )
                    }
            );
        }

        // Group data by grid box
        Map<String, List<Double>> scoreByBoxKey = new HashMap<>();
        for (NasaData ns : nasaData) {
            int latIdx = (int) Math.floor((ns.getLat() - gridLatMin) / latLonRange);
            int lonIdx = (int) Math.floor((ns.getLon() - gridLonMin) / latLonRange);
            String boxKey = latIdx + "," + lonIdx;
            scoreByBoxKey.computeIfAbsent(boxKey, k -> new ArrayList<>()).add(ns.getValue());
        }

        // Calculate average value for each box and create GeoFeatures
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

                // Calculate center coordinates of the box
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

        return new GeoResponse(
                GeoFeatureType.FEATURE_COLLECTION.asPascalCase(),
                features.stream()
                        .sorted((n1, n2) ->
                                (int) ((double) n1.properties().value() * 100 - (double) n2.properties().value() * 100))
                        .toArray(GeoFeature[]::new)
        );
    }
}
