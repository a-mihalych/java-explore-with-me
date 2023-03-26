package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        return StatsMapper.toEndpointHitDto(statsRepository.save(StatsMapper.toEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> hits(String start, String end, List<String> uris, Boolean unique) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(start, dateTimeFormatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, dateTimeFormatter);
        List<ViewStats> statsHits;
        if (unique) {
            statsHits = statsRepository.statsHitUnique(startDateTime, endDateTime, uris);
        } else {
            statsHits = statsRepository.statsHitNotUnique(startDateTime, endDateTime, uris);
        }
        return statsHits.stream()
                        .map(StatsMapper::toViewStatsDto)
                        .collect(Collectors.toList());
    }
}
