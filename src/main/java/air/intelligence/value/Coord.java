package air.intelligence.value;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class Coord {
    private double lat;
    private double lon;

    public static Coord of(double lat, double lon) {
        return new Coord(lat, lon);
    }
}
