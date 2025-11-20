package study.reviewscollector.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.service.HistoryService;

@RestController
@RequestMapping("api/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class HistoryController {

    private final HistoryService service;

    @GetMapping("getAll")
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(service.getHistory(token));


    }

}
