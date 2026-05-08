package study.reviewscollector.service.parsers;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import study.reviewscollector.dto.ParseResultDTO;
import study.reviewscollector.service.ParserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static study.reviewscollector.service.ParserService.REVIEWS_COUNT_SELECTORS;
import static study.reviewscollector.service.ParserService.RATINGS_SELECTORS;
import static study.reviewscollector.service.ParserService.NAME_SELECTORS;
import static study.reviewscollector.service.ParserService.randomDelay;

//TODO 2gis api?

@Service
@RequiredArgsConstructor
public class JsoupParserService {

    private final SeleniumParserService seleniumService;

    private static final Map<String, LocalDateTime> CIRCUIT_BREAKER = new ConcurrentHashMap<>();




    @Async("jsoupExecutor")
    @Transactional
    public CompletableFuture<ParseResultDTO> parse(String source, String url) {
        try {

            if (isSourceBlocked(source)) {
                return CompletableFuture.completedFuture(ParseResultDTO.builder()
                        .url(url)
                        .errorMessage("Источник заблокирован")
                        .build());
            }


            for (int i = 0; i < 3; i++) {
                try {
                    randomDelay();

                    String randomAgent = ParserService.randomAgent();
                    String randomReferer = ParserService.randomReferer();



                    Document doc = Jsoup.connect(url)
                            .userAgent(randomAgent)
                            .referrer(randomReferer)
                            .timeout(15000)
                            .get();

                    String name = extractName(doc, source);
                    Double rating = extractRating(doc, source);
                    Integer reviewsCount = extractReviewsCount(doc, source);




                    if (rating == null || reviewsCount == null || name == null) {
                        try { Thread.sleep(i * 500L); }
                        catch (InterruptedException ignored) {}
                        continue;
                    }

                    return CompletableFuture.completedFuture(ParseResultDTO.builder()
                            .url(url)
                            .name(name)
                            .source(source)
                            .rating(rating)
                            .reviewsCount(reviewsCount)
                            .build());
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

            CompletableFuture<List<ParseResultDTO>> res = seleniumService.parseBatch(new ArrayList<>(List.of(url)), source);
            if (res.get().get(0).getErrorMessage() == null)
                return CompletableFuture.completedFuture(res.get().get(0));
            else {
                tripCircuit(source);
                return CompletableFuture.completedFuture(ParseResultDTO.builder()
                        .url(url)
                        .errorMessage("Источник заблокирован")
                        .build());
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ParseResultDTO.builder()
                    .url(url)
                    .errorMessage("Возникла ошибка сервера")
                    .build());
        }
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
