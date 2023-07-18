package ru.practicum.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.error.exception.ExploreWithMeException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Stats {

    public static final String DATE_TIME_MAX = "3000-01-01 00:00:00";
    public static final String DATE_TIME_MIN = "2000-01-01 00:00:00";
    private final StatsClient statsClient;

    public void addHit(EndpointHitDto endpointHitDto) {
        statsClient.addHit(endpointHitDto);
    }

    public List<ViewStatsDto> hits(String start, String end, List<String> uris, Boolean unique) {
        Object o = statsClient.hits(start, end, uris, unique);
        ResponseEntity<Object> response = statsClient.hits(start, end, uris, unique);
        Gson gson = new Gson();
        String responseJson = gson.toJson(response.getBody());
        ViewStatsDto[] viewStatsDtos;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            viewStatsDtos = objectMapper.readValue(responseJson, ViewStatsDto[].class);
        } catch (IOException e) {
            throw new ExploreWithMeException("Ошибка сервера статистики");
        }
        return List.of(viewStatsDtos);
    }

    public Integer countHits(String start, String end, List<String> uris, Boolean unique) {
        List<ViewStatsDto> viewStatsDtos = hits(start, end, uris, unique);
        return (int) (viewStatsDtos.isEmpty() ? 0 : viewStatsDtos.get(0).getHits());
    }
}
