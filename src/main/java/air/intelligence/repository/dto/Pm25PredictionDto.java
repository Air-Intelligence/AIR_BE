package air.intelligence.repository.dto;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class Pm25PredictionDto {
    private double pred_pm25;
}
