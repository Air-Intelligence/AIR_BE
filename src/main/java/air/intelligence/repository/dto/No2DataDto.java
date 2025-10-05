package air.intelligence.repository.dto;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class No2DataDto {
    private double lat;
    private double lon;
    private double no2;
}
