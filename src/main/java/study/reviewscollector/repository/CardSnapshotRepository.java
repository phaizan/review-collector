package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.CardSnapshot;
import study.reviewscollector.entity.MonitoredCard;

import java.time.LocalDate;
import java.util.List;


public interface CardSnapshotRepository extends JpaRepository<CardSnapshot, Integer> {

    List<CardSnapshot> getCardSnapshotsByCardAndParsedAtBetweenOrderByParsedAt(MonitoredCard card, LocalDate parsedAtAfter, LocalDate parsedAtBefore);

    CardSnapshot getCardSnapshotByCardAndParsedAt(MonitoredCard card, LocalDate parsedAt);
}
