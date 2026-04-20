package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.dto.ParseResultDTO;
import study.reviewscollector.entity.User;
import study.reviewscollector.service.ParserService;

import java.util.List;

@RestController
@RequestMapping("api/parser")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ParserController {

    private final ParserService service;

    @PostMapping("run")
    public ResponseEntity<?> runParser(@RequestBody List<String> urls, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        List<ParseResultDTO> results = service.parseAll(urls, user);

        return ResponseEntity.ok(results);

    }
}
