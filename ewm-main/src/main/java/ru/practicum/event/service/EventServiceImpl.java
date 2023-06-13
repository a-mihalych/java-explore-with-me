package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.ExploreWithMeException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.error.exception.ValidationException;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventAdminStateAction;
import ru.practicum.event.model.EventPrivateStateAction;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.locate.mapper.LocateMapper;
import ru.practicum.locate.model.Locate;
import ru.practicum.locate.repository.LocateRepository;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.model.RequestStatus.CONFIRMED;
import static ru.practicum.request.model.RequestStatus.REJECTED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocateRepository locateRepository;
    private final RequestRepository requestRepository;
    private final StatsRepository statsRepository;

    @Override
    public EventFullDto eventById(Integer id, HttpServletRequest request) {
        Event event = getEvent(id);
        if (event.getEventState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id = %d не опубликовано", id));
        }
        request.getRemoteAddr();
        statsRepository.save(StatsMapper.toEndpointHit(new EndpointHitDto("ewm-main",
                                                                          request.getRequestURL().toString(),
                                                                          request.getRemoteAddr(),
                                                                          LocalDateTime.now())));
        return EventMapper.toEventFullDto(event,
                requestRepository.countRequestConfirmed(id),
                Math.toIntExact(statsRepository.viewStats("/events/" + event.getId()).getHits()));
    }

    @Override
    public List<EventShortDto> events(String text, List<Integer> categories, Boolean paid,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                      String sort, Integer from, Integer size, HttpServletRequest request) {
        if (rangeStart == null || rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            rangeEnd = null;
        }
        List<Event> events = eventRepository.events(text.toLowerCase(), categories, paid, rangeStart, rangeEnd,
                PageRequest.of(from / size, size));
        List<EventFullDto> eventFullDtos = events.stream()
                .map(event -> EventMapper.toEventFullDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        Math.toIntExact(statsRepository.viewStats("/events/" + event.getId()).getHits())))
                .collect(Collectors.toList());
        if (onlyAvailable) {
            eventFullDtos.stream().filter(event -> event.getConfirmedRequests() < event.getParticipantLimit());
        }
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        Math.toIntExact(statsRepository.viewStats("/events").getHits())))
                .collect(Collectors.toList());
        statsRepository.save(StatsMapper.toEndpointHit(new EndpointHitDto("ewm-main",
                                                                          request.getRequestURL().toString(),
                                                                          request.getRemoteAddr(),
                                                                          LocalDateTime.now())));
        if ("VIEWS".equals(sort)) {
            eventShortDtos.sort(Comparator.comparingInt(EventShortDto::getConfirmedRequests));
        }
        return eventShortDtos;
    }

    @Override
    public List<EventFullDto> eventsAdmin(List<Integer> users, List<String> states, List<Integer> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                          Integer from, Integer size) {
        return eventRepository.eventsAdmin(users, states, categories,
                                           rangeStart, rangeEnd, PageRequest.of(from / size, size))
                    .stream()
                    .map(event -> EventMapper.toEventFullDto(event,
                            requestRepository.countRequestConfirmed(event.getId()),
                            Math.toIntExact(statsRepository.viewStats("/admin/events").getHits())))
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Integer eventId, UpdateEventRequest updateEventRequest) {
        Event event = getEvent(eventId);
        Category category = null;
        if (updateEventRequest.getCategory() != null) {
            category = getCategory(updateEventRequest.getCategory());
        }
        EventAdminStateAction eventAdminStateAction = null;
        if (updateEventRequest.getStateAction() != null) {
            eventAdminStateAction = EventAdminStateAction.valueOf(updateEventRequest.getStateAction());
            validationStatusAdmin(event, eventAdminStateAction);
        }
        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateEventRequest.getEventDate()));
        }
        if (EventAdminStateAction.PUBLISH_EVENT == eventAdminStateAction) {
            validationBeginEvent(event.getEventDate(), event.getPublishedOn(), 1,
                    "Дата и время начала события не может быть раньше, чем через час от даты публикации");
        }
        event = EventMapper.toEvent(event, updateEventRequest, category);
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countRequestConfirmed(eventId),
                Math.toIntExact(statsRepository.viewStats("/events/" + event.getId()).getHits()));
    }

    @Override
    public List<EventShortDto> eventsUserId(Integer userId, Integer from, Integer size) {
        getUser(userId);
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size)).stream()
                .map(event -> EventMapper.toEventShortDto(event,
                            requestRepository.countRequestConfirmed(event.getId()),
                            Math.toIntExact(statsRepository.viewStats("/users/{userId}/events").getHits())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Integer userId, NewEventDto newEventDto) {
        Category category = getCategory(newEventDto.getCategory());
        User initiator = getUser(userId);
        Locate locate = locateRepository.save(LocateMapper.toLocate(newEventDto.getLocation()));
        Event event = EventMapper.toEvent(newEventDto, category, initiator, locate);
        validationBeginEvent(event.getEventDate(), event.getCreatedOn(), 2,
                "Дата и время начала события не может быть раньше, чем через два часа от текущего момента");
        return EventMapper.toEventFullDto(eventRepository.save(event), 0, 0);
    }

    @Override
    public EventFullDto eventByIdForUserId(Integer userId, Integer eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Событие принадлежит другому пользователю");
        }
        return EventMapper.toEventFullDto(event,
                requestRepository.countRequestConfirmed(eventId),
                Math.toIntExact(statsRepository.viewStats("/events/" + event.getId()).getHits()));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Integer userId, Integer eventId, UpdateEventRequest updateEventRequest) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Событие принадлежит другому пользователю");
        }
        Category category = null;
        if (updateEventRequest.getCategory() != null) {
            category = getCategory(updateEventRequest.getCategory());
        }
        if (event.getEventState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить опубликованное событие");
        }
        EventPrivateStateAction eventPrivateStateAction = null;
        if (updateEventRequest.getStateAction() != null) {
            eventPrivateStateAction = EventPrivateStateAction.valueOf(updateEventRequest.getStateAction());
            event.setEventState(validationStatusUser(event.getEventState(), eventPrivateStateAction));
        }
        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateEventRequest.getEventDate()));
            validationBeginEvent(event.getEventDate(), LocalDateTime.now(), 2,
                    "Дата и время начала события не может быть раньше, чем через два часа от текущего момента");
        }
        event = EventMapper.toEvent(event, updateEventRequest, category);
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countRequestConfirmed(eventId),
                Math.toIntExact(statsRepository.viewStats("/events/" + event.getId()).getHits()));
    }

    @Override
    public List<ParticipationRequestDto> eventsForUserId(Integer userId, Integer eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Событие принадлежит другому пользователю");
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequestsEvent(Integer userId, Integer eventId,
                                                                    EventRequestStatusUpdateRequest newStatus) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Событие принадлежит другому пользователю");
        }
        List<Request> requests = requestRepository.findAllByIdIn(newStatus.getRequestIds());
        int countOld = requests.size();
        requests.stream()
                .filter(request -> "PENDING".equals(request.getStatus()))
                .collect(Collectors.toList());
        String requestNotPending = countOld > requests.size() ?
                "Cтатус можно изменить только у заявок, находящихся в состоянии ожидания\n" : "";
        switch (newStatus.getStatus()) {
            case "CONFIRMED":
                if (event.getParticipantLimit().equals(0) || !event.getRequestModeration()) {
                    return EventRequestStatusUpdateResult.builder()
                            .confirmedRequests(requests.stream()
                                    .map(RequestMapper::toParticipationRequestDto)
                                    .collect(Collectors.toList()))
                            .rejectedRequests(List.of())
                            .build();
                }
                int numberRequestConfirmed = requestRepository.countRequestConfirmed(eventId);
                for (Request request : requests) {
                    numberRequestConfirmed++;
                    if (numberRequestConfirmed > event.getParticipantLimit()) {
                        request.setStatus(REJECTED);
                    } else {
                        request.setStatus(CONFIRMED);
                    }
                    requestRepository.save(request);
                }
                String limit = "";
                if (numberRequestConfirmed > event.getParticipantLimit()) {
                    limit = "Нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие";
                }
                if (!limit.isBlank() || !requestNotPending.isBlank()) {
                    throw new ConflictException(requestNotPending + limit);
                }
                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(requests.stream()
                                .filter(request -> request.getStatus() == CONFIRMED)
                                .map(RequestMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .rejectedRequests(requests.stream()
                                .filter(request -> request.getStatus() == REJECTED)
                                .map(RequestMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .build();
            case "CANCELED":
            case "REJECTED":
                requests.stream()
                        .map(request -> {
                            request.setStatus(RequestStatus.valueOf(newStatus.getStatus()));
                            return requestRepository.save(request);
                        })
                        .collect(Collectors.toList());
                if (!requestNotPending.isBlank()) {
                    throw new ConflictException(requestNotPending);
                }
                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(List.of())
                        .rejectedRequests(requests.stream()
                                .map(RequestMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .build();
            default:
                throw new ExploreWithMeException("Не верный статус для изменения запроса");
        }
    }

    private Category getCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена категория с id = %d", categoryId));
        });
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден пользователь с id = %d", userId));
        });
    }

    private Event getEvent(Integer eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдено событие с id = %d", eventId));
        });
    }

    private void validationBeginEvent(LocalDateTime beginEvent, LocalDateTime compare, int hours, String err) {
        if (beginEvent.isBefore(compare.plusHours(hours))) {
            throw new ConflictException(err);
        }
    }

    private void validationStatusAdmin(Event event, EventAdminStateAction updateStatus) {
        if (EventAdminStateAction.PUBLISH_EVENT == updateStatus && event.getEventState() == EventState.PENDING) {
            event.setEventState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
        if (EventAdminStateAction.REJECT_EVENT == updateStatus && !(event.getEventState() == EventState.PUBLISHED)) {
            event.setEventState(EventState.CANCELED);
        }
    }

    private EventState validationStatusUser(EventState eventState, EventPrivateStateAction updateStatus) {
        if (EventPrivateStateAction.SEND_TO_REVIEW == updateStatus) {
            return EventState.PENDING;
        }
        if (EventPrivateStateAction.CANCEL_REVIEW == updateStatus) {
            return EventState.CANCELED;
        }
        return eventState;
    }
}
