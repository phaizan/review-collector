package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import study.reviewscollector.entity.Template;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.TemplateRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository repository;

    public Template createTemplate (Template template, Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String login = user.getLogin();
        template.setUserLogin(login);
        repository.save(template);
        return template;

    }

    public Template updateTemplate (Template tmp) {

        repository.save(tmp);
        return tmp;

    }

    public List<Template> getAllByUserLogin(Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String login = user.getLogin();
        return repository.getTemplatesByUserLoginOrderByIdAsc(login);

    }

    public void deleteTemplate(int id) {
        repository.deleteById(id);
    }
}
