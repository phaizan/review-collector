package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.NewTemplateDTO;
import study.reviewscollector.entity.Template;
import study.reviewscollector.repository.TemplateRepository;
import study.reviewscollector.repository.UserRepository;
import study.reviewscollector.util.JwtUtil;

//TODO Spring Security?

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public void save(NewTemplateDTO templateDTO, String token) {

        String login = jwtUtil.extractLogin(token);
        Integer id = userRepository.getUserByLogin(login).getId();

        Template template = Template.builder()
                .name(templateDTO.getName())
                .links(templateDTO.getLinks())
                .userId(id)
                .build();


        templateRepository.save(template);
    }
}
