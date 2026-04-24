package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.MonitoredCard;

import java.util.List;

public interface MonitoredCardRepository extends JpaRepository<MonitoredCard, Integer> {

    List<MonitoredCard> getAllByUser_IdAndUrlInOrderBySourceAscNameAsc(Integer id, List<String> links);

    MonitoredCard getMonitoredCardByUserIdAndUrl(Integer user_id, String url);
}
