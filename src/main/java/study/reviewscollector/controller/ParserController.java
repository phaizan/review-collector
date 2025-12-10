package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.entity.ParseResult;
import study.reviewscollector.service.ParserService;
import study.reviewscollector.util.JwtUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/parser")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ParserController {

    private final ParserService service;
    private final JwtUtil util;

    @PostMapping("run")
    public ResponseEntity<?> runParser(
            @RequestBody List<String> urls,
            @RequestHeader("Authorization") String token) {

        String userLogin = util.extractLogin(token);

        List<CompletableFuture<ParseResult>> futures = urls.stream()
                .map(url -> service.parse(url, userLogin))
                .toList();

        // Дожидаемся завершения всех задач
        List<ParseResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        return ResponseEntity.ok(results);

    }
}
