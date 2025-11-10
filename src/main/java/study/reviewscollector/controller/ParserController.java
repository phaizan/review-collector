package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.entity.ParsedData;
import study.reviewscollector.service.ParserService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/parser")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ParserController {

    private final ParserService service;

    @PostMapping("run")
    public ResponseEntity<?> runParser(@RequestBody List<String> urls) {

        List<CompletableFuture<ParsedData>> tasks = urls.stream()
                .map(service::parse)
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
