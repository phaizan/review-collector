package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.ParsedData;

import java.util.List;

public interface ParsedDataRepository extends JpaRepository<ParsedData, Integer> {

    List<ParsedData> getParsedDataByUserLogin(String userLogin);
}
