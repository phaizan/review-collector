package study.reviewscollector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParseResultDTO {

    private String url;

    private String name;

    private String source;

    private Double rating;

    private Integer reviewsCount;

    private String errorMessage;

}
