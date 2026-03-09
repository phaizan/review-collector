package study.reviewscollector.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginDTO {
    //TODO переименовать в LoginDTO

    private String login;

    private String password;

}
