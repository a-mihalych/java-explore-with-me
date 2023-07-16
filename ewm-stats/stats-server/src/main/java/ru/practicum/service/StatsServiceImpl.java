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
    public List<ViewStatsDto> hits(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Не заданы параметры начала или конца события");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Событие должно начинаться раньше его окончания");
        }
        List<ViewStats> statsHits;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                statsHits = statsRepository.statsHitNotUrisUnique(start, end);
            } else {
                statsHits = statsRepository.statsHitNotUrisNotUnique(start, end);
            }
        } else {
            if (unique) {
                statsHits = statsRepository.statsHitUnique(start, end, uris);
            } else {
                statsHits = statsRepository.statsHitNotUnique(start, end, uris);
            }
        }
        return statsHits.stream()
                        .map(StatsMapper::toViewStatsDto)
                        .collect(Collectors.toList());
    }
}
