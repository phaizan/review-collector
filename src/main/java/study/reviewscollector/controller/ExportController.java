package study.reviewscollector.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.dto.HistoryDTO;
import study.reviewscollector.service.ExportService;

import java.io.IOException;

@RestController
@RequestMapping("api/report")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService service;

    @PostMapping("create")
    public ResponseEntity<?> exportHistory(@RequestBody HistoryDTO request, Authentication authentication) throws IOException {

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(service.exportHistory(request, authentication));

    }

}
