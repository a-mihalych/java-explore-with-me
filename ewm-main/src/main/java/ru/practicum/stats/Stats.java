package ru.practicum.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Stats {

    private final StatsClient statsClient;

    public void addHit(EndpointHitDto endpointHitDto) {
        statsClient.addHit(endpointHitDto);
    }

    public List<ViewStatsDto> hits(String start,
                                   String end,
                                   List<String> uris,
                                   Boolean unique) {
        return (List<ViewStatsDto>) statsClient.hits(start, end, uris, unique);
    }
}
