package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.Template;

public interface TemplateRepository extends JpaRepository<Template, Integer> {
}