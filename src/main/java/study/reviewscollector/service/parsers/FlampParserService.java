package study.reviewscollector.service.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.FlampDTO;
import study.reviewscollector.dto.FlampDTO.FilialDTO;
import study.reviewscollector.dto.ParseResultDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
public class FlampParserService {

    public static void gis2() throws IOException {

        /*String[] split = url.split("/");
        String strFirmId = null;
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("firm")) {
                strFirmId = split[++i];
            }
        }
        if (strFirmId != null) {
            split = strFirmId.split("-");
            long firmId = Long.parseLong(split[1]);
            apiUrl += firmId;
        }*/

        String firmId = "70000001051689722";

        String url = "https://public-api.reviews.2gis.com/2.0/branches/" + firmId + "/reviews";

        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(50));
        params.put("offset", String.valueOf(0));
        params.put("is_advertiser", "false");
        params.put("fields", "meta.providers,meta.branch_rating,meta.branch_reviews_count,meta.total_count,reviews.hiding_reason,reviews.is_verified,reviews.emojis");
        params.put("without_my_first_review", "false");
        params.put("rated", "true");
        params.put("sort_by", "date_edited");
        params.put("key", "6e7e1929-4ea9-4a5d-8c05-d601860x389bd");
        params.put("locale", "ru_RU");

        Connection.Response response = Jsoup.connect(url)
                .data(params)
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute();

        String json = response.body();
    }

    @Async("jsoupExecutor")
    public CompletableFuture<ParseResultDTO> flamp(String url) {
        try {
            String[] split = url.split("-");
            if (split.length != 2) {
                return CompletableFuture.completedFuture(ParseResultDTO.builder()
                        .url(url)
                        .errorMessage("Неправильный формат ссылки")
                        .build());
            }
            long firmId = Long.parseLong(split[1]);

            String apiUrl = "https://flamp.ru/api/2.0/filials/" + firmId;
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Application", "YourApp");
            headers.put("Origin", "https://example.com");
            headers.put("Authorization", "Bearer 2b93f266f6a4df2bb7a196bb76dca60181ea3b37");
            headers.put("Accept", "application/json");
            headers.put("Referer", "https://example.com/");
            headers.put("Accept-Encoding", "gzip, deflate, br");

            Connection.Response response = Jsoup.connect(apiUrl)
                    .headers(headers)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .execute();

            String json = response.body();

            ObjectMapper mapper = new ObjectMapper();

            FlampDTO flampDTO = mapper.readValue(json, FlampDTO.class);
            FilialDTO filialDTO = flampDTO.getFilial();

            return CompletableFuture.completedFuture(ParseResultDTO.builder()
                    .url(filialDTO.getUrl())
                    .name(filialDTO.getName())
                    .source("flamp")
                    .rating(filialDTO.getRating())
                    .reviewsCount(filialDTO.getReviewsCount())
                    .build());
        }

        catch (Exception e) {
            return CompletableFuture.completedFuture(ParseResultDTO.builder()
                    .url(url)
                    .errorMessage("Ошибка парсинга: " + e.getMessage())
                    .build());
        }
    }
}
