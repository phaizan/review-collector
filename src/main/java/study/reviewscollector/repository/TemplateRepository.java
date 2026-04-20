package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.Template;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Integer> {

    List<Template> getTemplatesByUserLoginOrderByIdAsc(String login);
}