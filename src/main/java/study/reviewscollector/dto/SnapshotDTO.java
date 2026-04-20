package study.reviewscollector.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotDTO {

    private Double rating;

    private Integer reviewsCount;

    private LocalDate parsedAt;

}
