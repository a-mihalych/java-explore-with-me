package ru.practicum.service;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.EndpointHitDto;

import java.util.List;

public interface StatsService {

    EndpointHitDto addHit(EndpointHitDto statsNewDto);

    List<ViewStatsDto> hits(String start, String end, List<String> uris, Boolean unique);
}
