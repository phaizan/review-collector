package study.reviewscollector.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class AuthResponse {
    private String token;
    private String login;
    private String name;
}
