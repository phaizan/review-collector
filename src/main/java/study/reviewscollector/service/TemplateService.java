package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.NewTemplateDTO;
import study.reviewscollector.entity.Template;
import study.reviewscollector.repository.TemplateRepository;
import study.reviewscollector.repository.UserRepository;
import study.reviewscollector.util.JwtUtil;

import java.util.List;

//TODO Spring Security?

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final JwtUtil jwtUtil;

    public void save(NewTemplateDTO templateDTO, String token) {

        String login = jwtUtil.extractLogin(token);

        Template template = Template.builder()
                .name(templateDTO.getName())
                .links(templateDTO.getLinks())
                .userLogin(login)
                .build();


        templateRepository.save(template);
    }

    public List<Template> getAllByUserId(String token) {
        String login = jwtUtil.extractLogin(token);
        return templateRepository.getTemplatesByUserLogin(login);


    }
}
