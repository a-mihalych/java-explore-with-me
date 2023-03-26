package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    public EndpointHitDto addHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("* Запрос Post: добаление статистики {}", endpointHitDto);
        return statsService.addHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> hits(@RequestParam String start,
                                   @RequestParam String end,
                                   @RequestParam List<String> uris,
                                   @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("* Запрос Get: получение статистики: начало - {}, конец - {}, адреса - {}, уникалность - {}",
                 start, end, uris, unique);
        return statsService.hits(start, end, uris, unique);
    }
}
