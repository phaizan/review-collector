package study.reviewscollector.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.reviewscollector.dto.ParseResultDTO;
import study.reviewscollector.entity.CardSnapshot;
import study.reviewscollector.entity.MonitoredCard;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.CardSnapshotRepository;
import study.reviewscollector.repository.MonitoredCardRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsyncParserService {

    private final MonitoredCardRepository cardRepository;
    private final CardSnapshotRepository snapshotRepository;

    private static final Map<String, LocalDateTime> CIRCUIT_BREAKER = new ConcurrentHashMap<>();

    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edg/128.0.0.0 Chrome/128.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) OPR/112.0.0.0 Chrome/128.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4) Gecko/20100101 Firefox/132.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.156 YaBrowser/24.4.1.755 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 YaBrowser/24.4.1.755 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15A5341f Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/128.0.0.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 13; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 13; SAMSUNG SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/24.0 Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; rv:115.0) Gecko/20100101 Firefox/115.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.156 YaBrowser/24.4.1.755 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 YaBrowser/24.4.1.755 Mobile/15E148 Safari/604.1"
    );

    private static final List<String> REFFER_FROM = List.of(
            "https://www.google.com/",
            "https://www.google.ru/",
            "https://yandex.ru/",
            "https://ya.ru/",
            "https://dzen.ru/",
            "https://www.bing.com/",
            "https://search.brave.com/",
            "https://duckduckgo.com/",
            "https://vk.com/",
            "https://ok.ru/"
            //"https://t.me/"
    );

    private static final HashMap<String, String> REVIEWS_COUNT_SELECTORS = new HashMap<>(Map.of(
            "otzovik", ".votes",
            "irecommend", ".count",
            "otzyvru", "span.count",
            "2gis", "._1pfef7u ._jspzdm",
            "flamp", ".filial-rating__reviews",
            "yandex", ".business-header-rating-view__text"
    ));

    private static final HashMap<String, String> RATINGS_SELECTORS = new HashMap<>(Map.of(
            "otzovik", ".rating-score-2 > span:nth-child(2)",
            "irecommend", ".rating .rating",
            "otzyvru", ".average",
            "flamp", ".filial-rating__value",
            "2gis", "._1pfef7u ._y10azs",
            "yandex", ".business-header-rating-view .business-rating-badge-view__rating-text"
    ));

    private static final Map<String, String[]> NAME_SELECTORS = Map.of(
            "otzovik", new String[]{".fn"},
            "irecommend", new String[]{".fn"},
            "otzyvru", new String[]{".element_name"},
            "flamp", new String[]{".header-filial__name"},
            "2gis", new String[]{"h1._1x89xo5", "h1._1rehek"},
            "yandex", new String[]{".card-title-view__title-link", ".orgpage-header-view__header-wrapper .orgpage-header-view__header"}
    );
    @Async
    @Transactional
    public CompletableFuture<ParseResultDTO> parse(String url, User user) {
        try {

            String source = detectSource(url);

            if (source == null) {
                return CompletableFuture.completedFuture(ParseResultDTO.builder()
                        .url(url)
                        .errorMessage("Источник не поддерживается")
                        .build());
            }

            if (isSourceBlocked(source)) {
                return CompletableFuture.completedFuture(ParseResultDTO.builder()
                        .url(url)
                        .errorMessage("Источник заблокирован")
                        .build());
            }

            Integer userId = user.getId();

            for (int i = 0; i < 3; i++) {
                try {
                    randomDelay();

                    String randomAgent = USER_AGENTS.get(ThreadLocalRandom.current().nextInt(USER_AGENTS.size()));
                    String randomReferer = REFFER_FROM.get(ThreadLocalRandom.current().nextInt(REFFER_FROM.size()));

                    Document doc = Jsoup.connect(url)
                            .userAgent(randomAgent)
                            .referrer(randomReferer)
                            .timeout(15000)
                            .get();

                    Double rating = extractRating(doc, source);
                    Integer reviewsCount = extractReviewsCount(doc, source);
                    String name = extractName(doc, source);

                    if (rating == null || reviewsCount == null || name == null) {
                        try { Thread.sleep(i * 500L); }
                        catch (InterruptedException ignored) {}
                        continue;
                    }

                    LocalDate today = LocalDate.now();

                    MonitoredCard card = cardRepository.getMonitoredCardByUserIdAndUrl(userId, url);
                    if (card == null) {
                        card = MonitoredCard.builder()
                                .url(url)
                                .source(source)
                                .name(name)
                                .user(user)
                                .build();
                        cardRepository.save(card);
                    } else {
                        card.setName(name);
                    }

                    CardSnapshot snapshot = snapshotRepository.getCardSnapshotByCardAndParsedAt(card, today);

                    if (snapshot == null) {
                        snapshot = CardSnapshot.builder()
                                .card(card)
                                .parsedAt(today)
                                .build();
                    }

                    snapshot.setRating(rating);
                    snapshot.setReviewsCount(reviewsCount);

                    snapshotRepository.save(snapshot);

                    ParseResultDTO resultDTO = ParseResultDTO.builder()
                            .url(url)
                            .name(name)
                            .source(source)
                            .rating(rating)
                            .reviewsCount(reviewsCount)
                            .build();

                    return CompletableFuture.completedFuture(resultDTO);
                }
                catch (Exception e) {
                    if (i == 2) {
                        return CompletableFuture.completedFuture(ParseResultDTO.builder()
                                .url(url)
                                .errorMessage("Ошибка парсинга: " + e.getMessage())
                                .build());
                    }
                }
            }

            tripCircuit(source);
            return CompletableFuture.completedFuture(ParseResultDTO.builder()
                    .url(url)
                    .errorMessage("Источник заблокирован")
                    .build());
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ParseResultDTO.builder()
                    .url(url)
                    .errorMessage("Возникла ошибка сервера")
                    .build());
        }
    }

    private String detectSource(String url) {
        HashSet<String> sources = new HashSet<>(Set.of("otzovik", "irecommend", "otzyvru", "flamp", "2gis", "yandex"));
        return sources.stream()
                .filter(url::contains)
                .findFirst().orElse(null);

    }

    private Double extractRating(Document doc, String source) {

        String text = doc.select(RATINGS_SELECTORS.get(source)).text();
        if (!text.isEmpty())
            return Double.parseDouble(text.replace(",", "."));
        return null;

    }

    private Integer extractReviewsCount(Document doc, String source) {

        String text = doc.select(REVIEWS_COUNT_SELECTORS.get(source)).text();
        if (!text.isEmpty()){
            String[] split = text.split(" ");
            if (source.equals("yandex") && split.length == 1)
                return Integer.parseInt(text.substring(1, text.length() - 1));
            return Integer.parseInt(split[0].replace(",", "."));
        }
        return null;

    }

    private String extractName(Document doc, String source) {

        for (String selector : NAME_SELECTORS.get(source)) {
            String text = doc.select(selector).text();
            if (!text.isBlank()){
                if (!source.equals("otzyvru"))
                    return text;
                else
                    return Arrays.stream(text.split(" "))
                            .filter(s -> !s.equals("отзывы"))
                            .collect(Collectors.joining(" "));
            }
        }

        return null;
    }

    private static void randomDelay() {
        try {
            int delay = ThreadLocalRandom.current().nextInt(300, 900);
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}
    }

    private boolean isSourceBlocked(String source) {
        LocalDateTime until = CIRCUIT_BREAKER.get(source);
        if (until == null) return false; // нет записи — не заблокирован
        if (until.isAfter(LocalDateTime.now())) {
            return true; // источник заблокирован
        } else {
            CIRCUIT_BREAKER.remove(source); // время прошло — удаляем запись
            return false;
        }
    }

    private void tripCircuit(String source) {
        CIRCUIT_BREAKER.put(source, LocalDateTime.now().plusMinutes(1));
    }
}
