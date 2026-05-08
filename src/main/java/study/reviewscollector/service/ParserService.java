package study.reviewscollector.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.ParseResultDTO;
import study.reviewscollector.entity.CardSnapshot;
import study.reviewscollector.entity.MonitoredCard;
import study.reviewscollector.entity.User;
import study.reviewscollector.repository.CardSnapshotRepository;
import study.reviewscollector.repository.MonitoredCardRepository;
import study.reviewscollector.service.parsers.ApiParserService;
import study.reviewscollector.service.parsers.JsoupParserService;
import study.reviewscollector.service.parsers.SeleniumParserService;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

//TODO Сделать проверку на существующее имя шаблона
//TODO удалить записи парсинга, которые были в один день


@Service
@RequiredArgsConstructor
public class ParserService {

    private final JsoupParserService jsoupService;
    private final SeleniumParserService seleniumService;
    private final ApiParserService flampService;

    private final MonitoredCardRepository cardRepository;
    private final CardSnapshotRepository snapshotRepository;


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

    public static final HashMap<String, String> REVIEWS_COUNT_SELECTORS = new HashMap<>(Map.of(
            "otzovik", ".votes",
            "irecommend", ".count",
            "otzyvru", "span.count",
            "2gis", "._1pfef7u ._jspzdm",
            "flamp", ".filial-rating__reviews",
            "yandex", ".sidebar-view__panel .business-header-rating-view__text"
    ));

    public static final HashMap<String, String> RATINGS_SELECTORS = new HashMap<>(Map.of(
            "otzovik", ".rating-score-2 > span:nth-child(2)",
            "irecommend", ".rating .rating",
            "otzyvru", ".average",
            "flamp", ".filial-rating__value",
            "2gis", "._1pfef7u ._y10azs",
            "yandex", ".sidebar-view__panel .business-header-rating-view .business-rating-badge-view__rating-text"
    ));

    public static final Map<String, String[]> NAME_SELECTORS = Map.of(
            "otzovik", new String[]{".fn"},
            "irecommend", new String[]{".fn"},
            "otzyvru", new String[]{".element_name"},
            "flamp", new String[]{".header-filial__name"},
            "2gis", new String[]{"h1._1x89xo5", "h1._1rehek"},
            "yandex", new String[]{".card-title-view__title-link", ".card-title-view__title", ".orgpage-header-view__header-wrapper .orgpage-header-view__header"}
    );

    public List<ParseResultDTO> parseAll(List<String> urls, User user) {

        //JsoupParserService.gis2();

        HashMap<String, List<String>> map = new HashMap<>();
        Set<String> seleniumSources = Set.of("yandex", "ozon");

        for (String url : urls) {
            String source = detectSource(url);
            map.computeIfAbsent(source, k -> new ArrayList<>()).add(url);
        }

        List<CompletableFuture<ParseResultDTO>> futures = new ArrayList<>();
        List<CompletableFuture<List<ParseResultDTO>>> listFutures = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            randomDelay();

            String source = entry.getKey();
            List<String> sourceUrls = entry.getValue();

            if (seleniumSources.contains(source)) {
                for (List<String> batch : partition(sourceUrls, 2)) {
                    randomDelay();
                    listFutures.add(seleniumService.parseBatch(batch, source));
                }
            } else {
                for (String url : sourceUrls) {
                    randomDelay();

                    try {
                        if ("flamp".equals(source)) {
                            futures.add(flampService.flamp(url));
                        }
                        else {
                            futures.add(jsoupService.parse(source, url));
                        }
                    } catch (Exception e) {
                        System.out.println("URL - " + url);
                        System.out.println("ERROR " + e.getMessage());
                    }

                /*if ("2gis".equals(source)) {
                    MonitoredCard card = cardRepository.getMonitoredCardByUserIdAndUrl(user.getId(), url);
                    flampService.flamp(url, card.getOrganizationId());
                }*/
                }
            }
        }


        List<ParseResultDTO> results = new ArrayList<>();

        results.addAll(futures.stream().map(CompletableFuture::join).toList());
        results.addAll(listFutures.stream().map(CompletableFuture::join).flatMap(List::stream).toList());

        saveAll(results, user);

        return results;
    }

    @Transactional
    protected void saveAll(List<ParseResultDTO> parseResultDTOs, User user) {

        Integer userId = user.getId();

        LocalDate today = LocalDate.now();

        for (ParseResultDTO parseResult : parseResultDTOs)
        {
            if (parseResult == null || parseResult.getErrorMessage() != null)
                continue;


            String source = parseResult.getSource();
            String url = parseResult.getUrl();
            String name = parseResult.getName();
            Double rating = parseResult.getRating();
            Integer reviewsCount = parseResult.getReviewsCount();

            MonitoredCard card = cardRepository.getMonitoredCardByUserIdAndUrl(userId, url);
            if (card == null) {
                card = MonitoredCard.builder()
                        .url(url)
                        .source(source)
                        .name(name)
                        .user(user)
                        .build();
                cardRepository.save(card);
            } else if (name != null && !name.isEmpty()) {
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
        }

    }


    private String detectSource(String url) {
        HashSet<String> sources = new HashSet<>(Set.of("otzovik", "irecommend", "otzyvru", "flamp", "2gis", "yandex", "ozon"));
        return sources.stream()
                .filter(url::contains)
                .findFirst().orElse(null);

    }

    public static void randomDelay() {
        try {
            int delay = ThreadLocalRandom.current().nextInt(300, 1200);
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}
    }

    private List<List<String>> partition(List<String> urls, int batchSize) {
        List<List<String>> result = new ArrayList<>();

        for (int i = 0; i < urls.size(); i += batchSize) {
            result.add(new ArrayList<>(urls.subList(i, Math.min(i + batchSize, urls.size()))));
        }

        return result;
    }

    public static String randomAgent() {
        return USER_AGENTS.get(ThreadLocalRandom.current().nextInt(USER_AGENTS.size()));
    }

    public static String randomReferer() {
        return REFFER_FROM.get(ThreadLocalRandom.current().nextInt(REFFER_FROM.size()));
    }

}
