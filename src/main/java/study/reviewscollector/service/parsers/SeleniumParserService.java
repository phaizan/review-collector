package study.reviewscollector.service.parsers;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.ParseResultDTO;
import study.reviewscollector.service.ParserService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static study.reviewscollector.service.ParserService.*;

@Service
@RequiredArgsConstructor
public class SeleniumParserService {

    @Async("seleniumExecutor")
    public CompletableFuture<List<ParseResultDTO>> parseBatch (List<String> urls, String source) {

        List<ParseResultDTO> results = new ArrayList<>();

        String userAgent = ParserService.randomAgent();
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("user-agent=" + userAgent);

        WebDriver driver = new ChromeDriver(options);

        try {
            for (String url : urls) {

                randomDelay();

                ParseResultDTO result = parse(driver, url, source);
                results.add(result);

                randomDelay();
            }

        }

        finally {
            driver.quit();
        }


        return CompletableFuture.completedFuture(results);
    }

    public ParseResultDTO parse(WebDriver driver, String url, String source) {

        try {
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            String name = findFirstNonBlankText(wait, NAME_SELECTORS.get(source));
            Double rating = Double.parseDouble(driver.findElement(By.cssSelector(RATINGS_SELECTORS.get(source))).getText().replace(',','.'));
            Integer reviewsCount = Integer.parseInt(driver.findElement(By.cssSelector(REVIEWS_COUNT_SELECTORS.get(source))).getText().split(" ")[0]);

            return ParseResultDTO.builder()
                    .url(url)
                    .name(name)
                    .source(source)
                    .rating(rating)
                    .reviewsCount(reviewsCount)
                    .build();
        } catch (Exception e) {
            return ParseResultDTO.builder()
                    .url(url)
                    .errorMessage("Ошибка парсинга: " + e.getMessage())
                    .build();
}

    }

    private String findFirstNonBlankText(WebDriverWait wait, String[] selectors) {
        for (String selector : selectors) {
            try {
                String text = wait.until(d -> {
                    List<WebElement> elements = d.findElements(By.cssSelector(selector));

                    for (WebElement element : elements) {
                        String value = element.getText();
                        if (value != null && !value.isBlank()) {
                            return value.trim();
                        }
                    }

                    return null;
                });

                if (text != null && !text.isBlank()) {
                    return text;
                }
            } catch (TimeoutException ignored) {
            }
        }

        return null;
    }


}
