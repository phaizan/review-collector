package study.reviewscollector.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> getTemplates(Authentication authentication) {
        return ResponseEntity.ok(service.getAllByUserLogin(authentication));
    }

    @PostMapping("create")
    public ResponseEntity<?> createTemplate(@RequestBody Template template, Authentication authentication) {
        return ResponseEntity.ok(service.createTemplate(template, authentication));
    }

    @Transactional
    @PostMapping("update")
    public ResponseEntity<?> updateTemplate(@RequestBody Template template) {
        return ResponseEntity.ok(service.updateTemplate(template));
    }

    @Transactional
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable int id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }
}
