package air.intelligence.repository.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class No2ResponseDto {
    private String time;
    private String variable;
    private long count;
    List<No2DataDto> data;
}
