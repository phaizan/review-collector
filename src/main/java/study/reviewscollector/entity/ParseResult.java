package study.reviewscollector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParseResult {

    private ParsedData parsedData;
    private LocalDateTime blockedUntil;
    private String error;

}
