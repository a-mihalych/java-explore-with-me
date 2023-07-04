package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController()
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> eventsAdmin(@RequestParam(required = false) List<Integer> users,
                                          @RequestParam(required = false) List<String> states,
                                          @RequestParam(required = false) List<Integer> categories,
                                          @RequestParam(required = false) LocalDateTime rangeStart,
                                          @RequestParam(required = false) LocalDateTime rangeEnd,
                                          @RequestParam(defaultValue = "0") Integer from,
                                          @RequestParam(defaultValue = "10") Integer size) {
        log.info("* Запрос Get: получение списка событий (Admin), " +
                 "users: {}, states: {}, categories: {}, rangeStart: {}, rangeEnd: {}, from: {}, size: {}",
                 users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.eventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventAdmin(@PathVariable Integer eventId,
                                         @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("* Запрос Patch: обновление события (Admin) с id = {}, событие: {}", eventId, updateEventRequest);
        return eventService.updateEventAdmin(eventId, updateEventRequest);
    }
}
