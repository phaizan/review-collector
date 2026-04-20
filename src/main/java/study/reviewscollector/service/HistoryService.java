package study.reviewscollector.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.CardDTO;
import study.reviewscollector.dto.HistoryRequestDTO;
import study.reviewscollector.dto.SnapshotDTO;
import study.reviewscollector.entity.CardSnapshot;
import study.reviewscollector.entity.MonitoredCard;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.CardSnapshotRepository;
import study.reviewscollector.repository.MonitoredCardRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CardSnapshotRepository snapshotRepository;
    private final MonitoredCardRepository cardRepository;

    public List<CardDTO> getHistory(HistoryRequestDTO request, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        List<MonitoredCard> cards = cardRepository.getAllByUser_IdAndUrlInOrderBySourceAscNameAsc(user.getId(), request.getLinks());

        List<CardDTO> cardDTOs = new ArrayList<>();

        for (MonitoredCard card : cards) {

            List<SnapshotDTO> snapshotDTOList = new ArrayList<>();

            List<CardSnapshot> cardSnapshots = snapshotRepository.getCardSnapshotsByCardAndParsedAtBetweenOrderByParsedAt(card, request.getDateFrom(), request.getDateTo());

            for (CardSnapshot cardSnapshot : cardSnapshots) {
                snapshotDTOList.add(SnapshotDTO.builder()
                        .rating(cardSnapshot.getRating())
                        .reviewsCount(cardSnapshot.getReviewsCount())
                        .parsedAt(cardSnapshot.getParsedAt())
                        .build());
            }

            CardDTO cardDTO = CardDTO.builder()
                    .url(card.getUrl())
                    .source(card.getSource())
                    .name(card.getName())
                    .snapshots(snapshotDTOList)
                    .build();

            cardDTOs.add(cardDTO);
        }

        return cardDTOs;
    }
}
