package study.reviewscollector.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryRequestDTO {

    private List<String> links;

    private LocalDate dateFrom;

    private LocalDate dateTo;

}
