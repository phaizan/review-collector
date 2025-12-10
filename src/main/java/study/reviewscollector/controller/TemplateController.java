package study.reviewscollector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.reviewscollector.entity.Template;
import study.reviewscollector.service.TemplateService;


@RestController
@RequestMapping("api/template")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TemplateController {

    private final TemplateService service;


    @GetMapping("get")
    public ResponseEntity<?> getTemplates(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(service.getAllByUserId(token));
    }

    @PostMapping("save")
    public ResponseEntity<?> saveTemplate(@RequestBody Template template, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(service.save(template, token));
    }
}
