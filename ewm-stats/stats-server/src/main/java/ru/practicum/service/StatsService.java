package ru.practicum.service;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHitDto addHit(EndpointHitDto statsNewDto);

    List<ViewStatsDto> hits(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
