package study.reviewscollector.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.dto.ParseResultDTO;
import study.reviewscollector.entity.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

//TODO Обеспечивать возможность удаления и обновления шаблонов мониторинга
//TODO Добавить ограничение ссылок на шаблон и на просто парсинг 50 шт
//TODO Переделать чтобы в ParsedData был не логин, а id пользователя
//TODO Сделать проверку на существующее имя шаблона

//TODO удалить записи парсинга, которые были в один день
//TODO сделать чтобы при парсинге в один день обновлялась последняя запись



//TODO Возможность планирования периодичности сбора данных (например, ежедневно, еженедельно).



@Service
@RequiredArgsConstructor
public class ParserService {

    private final AsyncParserService asyncParserService;

    public List<ParseResultDTO> parseAll(List<String> urls, User user) {
        List<CompletableFuture<ParseResultDTO>> futures = urls.stream()
                .map(url -> asyncParserService.parse(url, user))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

}
