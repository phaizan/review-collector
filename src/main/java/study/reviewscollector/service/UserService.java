package study.reviewscollector.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.AuthResponse;

@Service
@RequiredArgsConstructor
public class UserService {
    public static final String USER_DATA_SESSION_KEY = "UserData";

    public void getUser (HttpSession session) {
        AuthResponse user = (AuthResponse) session.getAttribute(USER_DATA_SESSION_KEY);
        System.out.println("Пользователь:" + user);
    }



}
