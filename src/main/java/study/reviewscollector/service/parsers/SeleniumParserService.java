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

        //options.addArguments("--headless=new");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("user-agent=" + userAgent);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            for (String url : urls) {

                for (int i = 0; i < 2; i++) {
                    randomDelay();

                    ParseResultDTO result = parse(driver, wait, url, source);
                    if (result.getErrorMessage() == null) {
                        results.add(result);
                        break;
                    }
                    if (i == 1) {
                        results.add(result);
                        break;
                    }
                    driver.quit();
                    driver = new ChromeDriver(options);
                    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                    randomDelay();

                }
            }
        }
        finally {
            driver.quit();
        }


        return CompletableFuture.completedFuture(results);
    }

    public ParseResultDTO parse(WebDriver driver, WebDriverWait wait, String url, String source) {

        for (int i = 0; i < 5; i++) {
            try {
                driver.get(url);

                if ("ozon".equals(source)) {
                    String text = wait.until(d -> d.findElement(By.cssSelector(".k6v_24 .ga5_3_16-a3")).getText());
                    System.out.println(text);
                    break;
                }

                String name = findFirstNonBlankText(wait, NAME_SELECTORS.get(source));
                Double rating = Double.parseDouble(wait.until(d -> d.findElement(By.cssSelector(RATINGS_SELECTORS.get(source))).getText().replace(',','.')));
                Integer reviewsCount = Integer.parseInt((wait.until(d -> d.findElement(By.cssSelector(REVIEWS_COUNT_SELECTORS.get(source))).getText().split(" ")[0])));

                if (name != null) {
                    return ParseResultDTO.builder()
                            .url(url)
                            .name(name)
                            .source(source)
                            .rating(rating)
                            .reviewsCount(reviewsCount)
                            .build();
                }

            } catch (Exception e) {
                System.out.println("Ошибка в ссылке " + url + "\n" + e);
            }
        }
        return ParseResultDTO.builder()
                .url(url)
                .errorMessage("Ошибка парсинга")
                .build();

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
