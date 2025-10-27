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

    @Async
    public CompletableFuture<ParsedData> parse(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
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
        HashSet<String> sources = new HashSet<>(Set.of("2gis", "otzovik", "irecommend"));
        return sources.stream()
                        .filter(url::contains)
                        .findFirst().orElse(null);

    }

    private Double extractRating(Document doc, String source) {
        HashMap<String, String> ratings = new HashMap<>(Map.of(
                "2gis", "._y10azs",
                "otzovik", ".rating-score-2 > span:nth-child(2)",
                "irecommend", ".rating .rating"
        ));
        return Double.parseDouble(doc.select(ratings.get(source)).text().replace(",", "."));
    }

    private Integer extractReviewsCount(Document doc, String source) {
        HashMap<String, String> ratings = new HashMap<>(Map.of(
                "2gis", "._jspzdm",
                "otzovik", ".votes",
                "irecommend", ".count"

        ));
        return Integer.parseInt(doc.select(ratings.get(source)).text().split(" ")[0].replace(",", "."));
    }


}
