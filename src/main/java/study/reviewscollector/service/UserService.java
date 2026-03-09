package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.LoginDTO;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.UserRepository;
import study.reviewscollector.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final JwtUtil util;

    public String registr (User user) {

        //TODO поменять user на dto
        if(repository.getUserByLogin(user.getLogin()) != null) {
            return null;
        }

        //TODO шифрование пароля

        repository.save(user);

        return user.getLogin();
    }

    public String login (LoginDTO loginDTO) {

        User user = repository.getUserByLogin(loginDTO.getLogin());

        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            return null;
        }

        return util.generateToken(user);
    }
}
