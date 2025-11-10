package study.reviewscollector.service;


import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import study.reviewscollector.entity.ParsedData;
import study.reviewscollector.repository.ParsedDataRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ParserService {

    private final ParsedDataRepository repository;

    private static final List<String> USER_AGENTS = List.of(
            // Chrome Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",

            // Edge Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",

            // Firefox Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0",

            // Safari macOS
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15"
    );

    private static final List<String> REFFER_FROM = List.of(

            "https://www.google.com/"

    );

    @Async
    public CompletableFuture<ParsedData> parse(String url) {
        try {
            String randomAgent = USER_AGENTS.get(new Random().nextInt(USER_AGENTS.size()));
            String randomReferer = REFFER_FROM.get(new Random().nextInt(REFFER_FROM.size()));
            Document doc = Jsoup.connect(url)
                    .userAgent(randomAgent)
                    .referrer(randomReferer)
                    .timeout(15000)
                    .get();


            String source = detectSource(url);
            if (source == null)
                return CompletableFuture.completedFuture(null);

            Double rating = extractRating(doc, source);
            Integer reviewsCount = extractReviewsCount(doc, source);

            ParsedData parsedData = ParsedData.builder()
                    .url(url)
                    .source(source)
                    .rating(rating)
                    .reviewsCount(reviewsCount)
                    .parsedAt(LocalDateTime.now())
                    .build();

            repository.save(parsedData);

            return CompletableFuture.completedFuture(parsedData);

        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }

    }

    private String detectSource(String url) {
        HashSet<String> sources = new HashSet<>(Set.of("otzovik", "irecommend", "otzyvru", "flamp", "2gis", "yandex")); //вернуть вк
        return sources.stream()
                        .filter(url::contains)
                        .findFirst().orElse(null);

    }

    private Double extractRating(Document doc, String source) {
        HashMap<String, String> ratings = new HashMap<>(Map.of(
                "otzovik", ".rating-score-2 > span:nth-child(2)",
                "irecommend", ".rating .rating",
                "otzyvru", ".average",
                "flamp", ".filial-rating__value",
                "2gis", "._1pfef7u ._y10azs",
                "yandex", ".business-header-rating-view .business-rating-badge-view__rating-text"
        ));
        String text = doc.select(ratings.get(source)).text();
        if (!text.isEmpty())
            return Double.parseDouble(text.replace(",", "."));
        return null;
    }

    private Integer extractReviewsCount(Document doc, String source) {
        HashMap<String, String> reviewsCount = new HashMap<>(Map.of(
                "otzovik", ".votes",
                "irecommend", ".count",
                "otzyvru", "span.count",
                "2gis", "._1pfef7u ._jspzdm",
                "flamp", ".filial-rating__reviews",
                "yandex", ".business-header-rating-view .business-header-rating-view__text"
        ));
        String text = doc.select(reviewsCount.get(source)).text();
        if (!text.isEmpty()){
            if (!source.equals("vk.com"))
                return Integer.parseInt(text.split(" ")[0].replace(",", "."));
            return Integer.parseInt(text.split(" ")[2].replace(",", "."));
        }
        return null;
    }



}
