package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto eventById(Integer id, HttpServletRequest request);

    List<EventShortDto> events(String text, List<Integer> categories, Boolean paid,
                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                               String sort, Integer from, Integer size, HttpServletRequest request);

    List<EventFullDto> eventsAdmin(List<Integer> users, List<String> states, List<Integer> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Integer eventId, UpdateEventRequest updateEventRequest);

    List<EventShortDto> eventsUserId(Integer userId, Integer from, Integer size);

    EventFullDto createEvent(Integer userId, NewEventDto newEventDto);

    EventFullDto eventByIdForUserId(Integer userId, Integer eventId);

    EventFullDto updateEvent(Integer userId, Integer eventId, UpdateEventRequest updateEventRequest);

    List<ParticipationRequestDto> eventsForUserId(Integer userId, Integer eventId);

    EventRequestStatusUpdateResult updateStatusRequestsEvent(Integer userId, Integer eventId,
                                                             EventRequestStatusUpdateRequest statusUpdateRequest);
}
