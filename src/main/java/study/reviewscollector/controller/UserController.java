package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.dto.LoginRequestDTO;
import study.reviewscollector.dto.UserDTO;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.UserRepository;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("registration")
    public ResponseEntity<?> registr (@RequestBody User user) {

        if(userRepository.getUserByLogin(user.getLogin()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Такой логин уже используется");
        }

        userRepository.save(user);

        return ResponseEntity.ok(new UserDTO()
                .setId(user.getId())
                .setName(user.getName()));
    }

    @PostMapping("login")
    public ResponseEntity<?> login (@RequestBody LoginRequestDTO loginRequestDTO) {

        User user = userRepository.getUserByLogin(loginRequestDTO.getLogin());
        if (user == null || !user.getPassword().equals(loginRequestDTO.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Введен неправильный логин и пароль");
        }

        return ResponseEntity.ok(new UserDTO()
                .setId(user.getId())
                .setName(user.getName()));

    }

}
