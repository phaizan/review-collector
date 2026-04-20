package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.MonitoredCard;

import java.util.List;

public interface MonitoredCardRepository extends JpaRepository<MonitoredCard, Integer> {

    MonitoredCard getMonitoredCardByUrl(String url);

    boolean existsByUrl(String url);

    List<MonitoredCard> getAllByUser_IdAndUrlInOrderBySourceAscNameAsc(Integer id, List<String> links);
}
