package study.reviewscollector.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.dto.HistoryRequestDTO;
import study.reviewscollector.service.ReportService;

import java.io.IOException;

@RestController
@RequestMapping("api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @PostMapping("create")
    public ResponseEntity<?> createReport(@RequestBody HistoryRequestDTO request, Authentication authentication) throws IOException {

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(service.createReport(request, authentication));

    }

}
