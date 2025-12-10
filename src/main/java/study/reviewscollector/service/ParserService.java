package study.reviewscollector.service;


import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import study.reviewscollector.entity.ParseResult;
import study.reviewscollector.entity.ParsedData;
import study.reviewscollector.repository.ParsedDataRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

//TODO Возможность планирования периодичности сбора данных (например, ежедневно, еженедельно).
//TODO Проверку корректности URL-адресов, добавляемых пользователем.
//TODO Обеспечивать возможность удаления и обновления шаблонов мониторинга
//TODO Сделать проверку на токен, чтобы выходило из системы если токен expired
//TODO Добавить ограничение ссылок на шаблон и на просто парсинг 50 шт
//TODO Переделать чтобы в ParsedData был не логин, а id пользователя
//TODO Сделать проверку на существующее имя шаблона
//TODO Добавить кол-во ссылок когда произошла ошибка на фронте
//TODO Spring Security?
//TODO История парсинга не по порядку даты идет
//TODO Добавить название карточки (название продукта)
//TODO


@Service
@RequiredArgsConstructor
public class ParserService {

    private final ParsedDataRepository repository;

    private static final Map<String, LocalDateTime> CIRCUIT_BREAKER = new ConcurrentHashMap<>();

    private static final List<String> USER_AGENTS = List.of(

            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
            // Chrome Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",

            // Edge Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",

            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0",
            // Firefox Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0",

            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edg/128.0.0.0 Chrome/128.0.0.0 Safari/537.36",

            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) OPR/112.0.0.0 Chrome/128.0.0.0 Safari/537.36",

            // Safari macOS
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
            "https://ok.ru/",
            "https://t.me/"

    );

    private static final HashMap<String, String> REVIEWS_COUNT_SELECTORS = new HashMap<>(Map.of(
            "otzovik", ".votes",
            "irecommend", ".count",
            "otzyvru", "span.count",
            "2gis", "._1pfef7u ._jspzdm",
            "flamp", ".filial-rating__reviews",
            "yandex", ".business-header-rating-view .business-header-rating-view__text"
    ));

    private static final HashMap<String, String> RATINGS_SELECTORS = new HashMap<>(Map.of(
            "otzovik", ".rating-score-2 > span:nth-child(2)",
            "irecommend", ".rating .rating",
            "otzyvru", ".average",
            "flamp", ".filial-rating__value",
            "2gis", "._1pfef7u ._y10azs",
            "yandex", ".business-header-rating-view .business-rating-badge-view__rating-text"
    ));

    @Async
    public CompletableFuture<ParseResult> parse(String url, String userLogin) {
        try {

            String source = detectSource(url);

            if (source == null) {
                return CompletableFuture.completedFuture(ParseResult.builder()
                        .parsedData(ParsedData.builder()
                                .url(url)
                                .build())
                        .error("Источник не поддерживается")
                        .build());
            }

            if (isSourceBlocked(source)) {
                return CompletableFuture.completedFuture(ParseResult.builder()
                        .parsedData(ParsedData.builder()
                                .url(url)
                                .build())
                        .error("Источник заблокирован")
                        .blockedUntil(CIRCUIT_BREAKER.get(source))
                        .build());
            }


            for (int i = 0; i < 3; i++)
            {
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

                if (rating == null || reviewsCount == null) {
                    try { Thread.sleep(i * 500L); }
                    catch (InterruptedException ignored) {}
                    continue;
                }

                ParsedData parsedData = ParsedData.builder()
                        .url(url)
                        .source(source)
                        .rating(rating)
                        .reviewsCount(reviewsCount)
                        .parsedAt(LocalDateTime.now())
                        .userLogin(userLogin)
                        .build();

                repository.save(parsedData);

                return CompletableFuture.completedFuture(ParseResult
                        .builder()
                        .parsedData(parsedData)
                        .build());
            }

            tripCircuit(source);
            return CompletableFuture.completedFuture(ParseResult.builder()
                    .parsedData(ParsedData.builder()
                            .url(url)
                            .build())
                    .error("Источник заблокирован")
                    .blockedUntil(CIRCUIT_BREAKER.get(source))
                    .build());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(ParseResult.builder()
                .parsedData(ParsedData.builder()
                        .url(url)
                        .build())
                .error("Возникла ошибка сервера")
                .build());
    }

    private String detectSource(String url) {
        HashSet<String> sources = new HashSet<>(Set.of("otzovik", "irecommend", "otzyvru", "flamp", "2gis", "yandex")); //вернуть вк
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
            if (!source.equals("vk.com"))
                return Integer.parseInt(text.split(" ")[0].replace(",", "."));
            return Integer.parseInt(text.split(" ")[2].replace(",", "."));
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
