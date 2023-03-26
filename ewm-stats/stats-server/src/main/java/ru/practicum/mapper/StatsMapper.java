package ru.practicum.mapper;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

public class StatsMapper {

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        return EndpointHit.builder()
                          .id(null)
                          .app(endpointHitDto.getApp())
                          .uri(endpointHitDto.getUri())
                          .ip(endpointHitDto.getIp())
                          .timestamp(endpointHitDto.getTimestamp())
                          .build();
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                             .app(endpointHit.getApp())
                             .uri(endpointHit.getUri())
                             .ip(endpointHit.getIp())
                             .timestamp(endpointHit.getTimestamp())
                             .build();
    }

    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                           .app(viewStats.getApp())
                           .uri(viewStats.getUri())
                           .hits(viewStats.getHits())
                           .build();
    }
}
