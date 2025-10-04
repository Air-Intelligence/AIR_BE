package air.intelligence.dto;

import air.intelligence.value.Coord;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class LastCoordUpdateRequest {

    @NotNull
    private String userId;

    private Coord coord;
}
