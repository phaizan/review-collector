package study.reviewscollector.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CardDTO {

    private String url;

    private String source;

    private String name;

    private List<SnapshotDTO> snapshots;

}
