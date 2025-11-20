package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.entity.ParsedData;
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
    private final JwtUtil jwtUtil;

    @PostMapping("run")
    public ResponseEntity<?> runParser(@RequestBody List<String> urls, @RequestHeader("Authorization") String token) {

        String userLogin = jwtUtil.extractLogin(token);

        List<CompletableFuture<ParsedData>> tasks = urls.stream()
                .map(url -> service.parse(url, userLogin))
                .toList();

        List<ParsedData> results = tasks.stream()
                .map(task -> {
                    try {
                        return task.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toList();
        return ResponseEntity.ok(results);
    }



}
