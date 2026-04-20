package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.LoginDTO;
import study.reviewscollector.dto.RegisterDTO;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.UserRepository;
import study.reviewscollector.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil util;

    public String register (RegisterDTO dto) {

        if(repository.getUserByLogin(dto.getLogin()) != null) {
            return null;
        }

        User user = User.builder()
                .login(dto.getLogin())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .build();

        repository.save(user);

        return dto.getLogin();
    }

    public String login (LoginDTO loginDTO) {

        User user = repository.getUserByLogin(loginDTO.getLogin());

        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return null;
        }

        return util.generateToken(user);
    }
}