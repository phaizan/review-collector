package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.ParsedData;

public interface ParsedDataRepository extends JpaRepository<ParsedData, Integer> {
}
