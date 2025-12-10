package study.reviewscollector.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.reviewscollector.entity.ParsedData;
import study.reviewscollector.repository.ParsedDataRepository;
import study.reviewscollector.util.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ParsedDataRepository repository;
    private final JwtUtil util;

    public List<ParsedData> getHistory(String token) {
        String userLogin = util.extractLogin(token);
        return repository.getParsedDataByUserLogin(userLogin);
    }

}
