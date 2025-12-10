package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.entity.Template;
import study.reviewscollector.repository.TemplateRepository;
import study.reviewscollector.util.JwtUtil;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository repository;
    private final JwtUtil util;

    public Template save (Template template, String token) {

        String login = util.extractLogin(token);

        template.setUserLogin(login);

        repository.save(template);

        return template;
    }

    public List<Template> getAllByUserId(String token) {
        String login = util.extractLogin(token);
        return repository.getTemplatesByUserLogin(login);
    }
}
