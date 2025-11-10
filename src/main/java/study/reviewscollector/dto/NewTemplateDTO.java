package study.reviewscollector.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class NewTemplateDTO {

    private String name;

    private Set<String> links;

}
