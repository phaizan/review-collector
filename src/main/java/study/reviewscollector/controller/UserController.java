package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.dto.LoginDTO;
import study.reviewscollector.dto.RegisterDTO;
import study.reviewscollector.service.UserService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserService service;

    @PostMapping("/registration")
    public ResponseEntity<?> registr (@RequestBody RegisterDTO dto) {
        String login = service.register(dto);

        if (login == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Такой логин уже используется");

        return ResponseEntity.ok(login);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginDTO dto) {

        String token = service.login(dto);

        if (token == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Введен неправильный логин и пароль");

        return ResponseEntity.ok(token);
    }

}
