package study.reviewscollector.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RegisterDTO {

    private String login;

    private String password;

    private String name;

}
