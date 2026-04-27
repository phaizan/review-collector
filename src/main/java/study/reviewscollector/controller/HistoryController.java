package study.reviewscollector.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.dto.HistoryDTO;
import study.reviewscollector.service.HistoryService;

@RestController
@RequestMapping("api/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class HistoryController {

    private final HistoryService service;

    @PostMapping("get")
    public ResponseEntity<?> get(@RequestBody HistoryDTO request, Authentication authentication) {

        return ResponseEntity.ok(service.getHistory(request, authentication));

    }

}
