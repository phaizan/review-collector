package study.reviewscollector.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.AuthResponse;
import study.reviewscollector.dto.LoginRequestDTO;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.UserRepository;
import study.reviewscollector.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserService {

    public static final String USER_DATA_SESSION_KEY = "UserData";

    private final UserRepository repository;
    private final JwtUtil util;

    public String registr (User user) {

        if(repository.getUserByLogin(user.getLogin()) != null) {
            return null;
        }
        repository.save(user);

        return user.getLogin();
    }

    public void getUserBySession (HttpSession session) {
        AuthResponse user = (AuthResponse) session.getAttribute(USER_DATA_SESSION_KEY);
        System.out.println("Пользователь:" + user);
    }

    public AuthResponse login (LoginRequestDTO loginRequestDTO) {

        User user = repository.getUserByLogin(loginRequestDTO.getLogin());

        if (user == null || !user.getPassword().equals(loginRequestDTO.getPassword())) {
            return null;
        }

        String token = util.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .login(user.getLogin())
                .name(user.getName())
                .build();
    }
}
