package study.reviewscollector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlampDTO {

        private Integer code;
        private String status;

        private FilialDTO filial;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class FilialDTO {
                private String id;
                private String name;
                private String url;

                @JsonProperty("reviews_count")
                private Integer reviewsCount;

                @JsonProperty("rating_decimal")
                private Double rating;
        }

}
