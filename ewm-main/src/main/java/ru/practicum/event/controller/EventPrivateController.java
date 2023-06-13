package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController()
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> eventsUserId(@PathVariable Integer userId,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("* Запрос Get: получение событий добавленных пользователем с id = {}, from: {}, size: {}",
                 userId, from, size);
        return eventService.eventsUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Integer userId, @RequestBody NewEventDto newEventDto) {
        log.info("* Запрос Post: создание события {}, пользователем с id = {}", newEventDto, userId);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto eventByIdForUserId(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("* Запрос Get: получение события по id = {}, для пользователя с id = {}", eventId, userId);
        return eventService.eventByIdForUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Integer userId,
                                    @PathVariable Integer eventId,
                                    @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("* Запрос Patch: обновление события с id = {}, пользоателем с id = {}, событие: {}",
                 eventId, userId, updateEventRequest);
        return eventService.updateEvent(userId, eventId, updateEventRequest);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> eventsForUserId(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("* Запрос Get: получение запросов на участие в событии с id = {}, созданное пользователем с id = {}",
                 eventId, userId);
        return eventService.eventsForUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatusRequestsEvent(
                                          @PathVariable Integer userId, @PathVariable Integer eventId,
                                          @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("* Запрос Patch: обновление статуса участия в событии с id = {}, пользоателя с id = {}, заявки: {}",
                 eventId, userId, statusUpdateRequest);
        return eventService.updateStatusRequestsEvent(userId, eventId, statusUpdateRequest);
    }
}
