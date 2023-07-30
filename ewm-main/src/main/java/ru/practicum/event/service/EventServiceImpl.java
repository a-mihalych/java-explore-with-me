package ru.practicum.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
import ru.practicum.event.model.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.locate.mapper.LocateMapper;
import ru.practicum.locate.model.Locate;
import ru.practicum.locate.repository.LocateRepository;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stats.Stats;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final RatingRepository ratingRepository;
    private final Stats stats;

    @Override
    public EventFullDto eventById(Integer id, HttpServletRequest request) {
        Event event = getEvent(id);
        if (event.getEventState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id = %d не опубликовано", id));
        }
        stats.addHit(EndpointHitDto.builder()
                        .app("ewm-main-service")
                        .uri("/events/" + id)
                        .ip(request.getRemoteAddr())
                        .timestamp(LocalDateTime.now())
                        .build());
        int countHits = stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                        List.of("/events/" + event.getId()), true);
        int like = ratingRepository.countRatingTrue(event.getId());
        int dislike = ratingRepository.countRatingFalse(event.getId());
        return EventMapper.toEventFullDto(event, requestRepository.countRequestConfirmed(id), countHits, like, dislike);
    }

    @Override
    public List<EventShortDto> events(String text, List<Integer> categories, Boolean paid,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                      String sort, Integer from, Integer size, HttpServletRequest request) {
        if (rangeStart == null || rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            rangeEnd = null;
        }
        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            throw new ValidationException("Событие должно начинаться раньше его окончания");
        }
        BooleanBuilder builder = new BooleanBuilder();
        if (text != null) {
            builder.and(QEvent.event.annotation.likeIgnoreCase("%" + text.toLowerCase() + "%"))
                   .or(QEvent.event.description.likeIgnoreCase("%" + text.toLowerCase() + "%"));
        }
        if (categories != null) {
            builder.and(QEvent.event.category.id.in(categories));
        }
        if (paid != null) {
            builder.and(QEvent.event.paid.eq(paid));
        }
        if (rangeStart != null) {
            builder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            builder.and(QEvent.event.eventDate.before(rangeEnd));
        }
        BooleanExpression booleanExpression = builder.getValue() == null ? QEvent.event.isNotNull() : Expressions.asBoolean(builder.getValue());
        List<Event> events = eventRepository.findAll(booleanExpression, PageRequest.of(from / size, size)).toList();
        List<EventFullDto> eventFullDtos = events.stream()
                .map(event -> EventMapper.toEventFullDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true),
                        ratingRepository.countRatingTrue(event.getId()),
                        ratingRepository.countRatingFalse(event.getId())))
                .collect(Collectors.toList());
        if (onlyAvailable) {
            eventFullDtos.stream().filter(event -> event.getConfirmedRequests() < event.getParticipantLimit());
        }
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true),
                        ratingRepository.countRatingTrue(event.getId()),
                        ratingRepository.countRatingFalse(event.getId())))
                .collect(Collectors.toList());
        stats.addHit(EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
        if ("VIEWS".equals(sort)) {
            eventShortDtos.sort(Comparator.comparingInt(EventShortDto::getConfirmedRequests));
        }
        return eventShortDtos;
    }

    @Override
    public List<EventFullDto> eventsAdmin(List<Integer> users, List<String> states, List<Integer> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                          Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            throw new ValidationException("Событие должно начинаться раньше его окончания");
        }
        BooleanBuilder builder = new BooleanBuilder();
        if (users != null) {
            builder.and(QEvent.event.initiator.id.in(users));
        }
        if (states != null) {
            states.forEach(status -> {
                try {
                    EventState.valueOf(status);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException(String.format("Статус события %s не является допустимым", status));
                }
            });
            builder.and(QEvent.event.eventState.in(states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList())));
        }
        if (categories != null) {
            builder.and(QEvent.event.category.id.in(categories));
        }
        if (rangeStart != null) {
            builder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            builder.and(QEvent.event.eventDate.before(rangeEnd));
        }
        BooleanExpression booleanExpression;
        if (builder.getValue() == null) {
            booleanExpression = QEvent.event.isNotNull();
        } else {
            booleanExpression = Expressions.asBoolean(builder.getValue());
        }
        List<Event> events = eventRepository.findAll(booleanExpression, PageRequest.of(from / size, size)).toList();
        return events.stream()
                     .map(event -> EventMapper.toEventFullDto(event,
                            requestRepository.countRequestConfirmed(event.getId()),
                            stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                    List.of("/events/" + event.getId()), true),
                             ratingRepository.countRatingTrue(event.getId()),
                             ratingRepository.countRatingFalse(event.getId())))
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
        LocalDateTime eventDate = validationBeginEventAfterNow(updateEventRequest.getEventDate());
        if (eventDate != null) {
            if (EventAdminStateAction.PUBLISH_EVENT == eventAdminStateAction) {
                validationBeginEvent(eventDate, event.getPublishedOn(), 1,
                        "Дата и время начала события не может быть раньше, чем через час от даты публикации");
            }
            event.setEventDate(eventDate);
        }
        validationSizeString(updateEventRequest.getAnnotation(),
                updateEventRequest.getDescription(), updateEventRequest.getTitle());
        event = EventMapper.toEvent(event, updateEventRequest, category);
        if (updateEventRequest.getLocation() != null) {
            Locate locate = locateRepository.save(LocateMapper.toLocate(updateEventRequest.getLocation()));
            event.setLocate(locate);
        }
        int countRequestConfirmed = requestRepository.countRequestConfirmed(eventId);
        int countHits = stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                List.of("/events/" + event.getId()), true);
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event, countRequestConfirmed, countHits,
                ratingRepository.countRatingTrue(event.getId()),
                ratingRepository.countRatingFalse(event.getId()));
    }

    @Override
    public List<EventShortDto> eventsUserId(Integer userId, Integer from, Integer size) {
        getUser(userId);
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size)).stream()
                .map(event -> EventMapper.toEventShortDto(event,
                            requestRepository.countRequestConfirmed(event.getId()),
                        stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true),
                        ratingRepository.countRatingTrue(event.getId()),
                        ratingRepository.countRatingFalse(event.getId())))
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
        return EventMapper.toEventFullDto(eventRepository.save(event), 0, 0, 0, 0);
    }

    @Override
    public EventFullDto eventByIdForUserId(Integer userId, Integer eventId) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Событие принадлежит другому пользователю");
        }
        return EventMapper.toEventFullDto(event,
                requestRepository.countRequestConfirmed(eventId),
                stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                        List.of("/events/" + event.getId()), true),
                ratingRepository.countRatingTrue(event.getId()),
                ratingRepository.countRatingFalse(event.getId()));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Integer userId, Integer eventId, UpdateEventRequest updateEventRequest) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
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
        LocalDateTime eventDate = validationBeginEventAfterNow(updateEventRequest.getEventDate());
        if (eventDate != null) {
            validationBeginEvent(event.getEventDate(), LocalDateTime.now(), 2,
                    "Дата и время начала события не может быть раньше, чем через два часа от текущего момента");
            event.setEventDate(eventDate);
        }
        validationSizeString(updateEventRequest.getAnnotation(),
                updateEventRequest.getDescription(), updateEventRequest.getTitle());
        event = EventMapper.toEvent(event, updateEventRequest, category);
        if (updateEventRequest.getLocation() != null) {
            Locate locate = locateRepository.save(LocateMapper.toLocate(updateEventRequest.getLocation()));
            event.setLocate(locate);
        }
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countRequestConfirmed(eventId),
                stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                        List.of("/events/" + event.getId()), true),
                ratingRepository.countRatingTrue(event.getId()),
                ratingRepository.countRatingFalse(event.getId()));
    }

    @Override
    public List<ParticipationRequestDto> eventsForUserId(Integer userId, Integer eventId) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
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
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Событие принадлежит другому пользователю");
        }
        List<Request> requests = requestRepository.findAllByIdIn(newStatus.getRequestIds());
        int countOld = requests.size();
        requests = requests.stream()
                .filter(request -> "PENDING".equals(request.getStatus().toString()))
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
            throw new ValidationException(err);
        }
    }

    private LocalDateTime validationBeginEventAfterNow(String beginEvent) {
        LocalDateTime eventDate = null;
        if (beginEvent != null) {
            eventDate = LocalDateTime.parse(beginEvent, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (!LocalDateTime.now().isBefore(eventDate)) {
                throw new ValidationException("Начало события должно быть в будущем");
            }
        }
        return eventDate;
    }

    private void validationStatusAdmin(Event event, EventAdminStateAction updateStatus) {
        if (EventAdminStateAction.PUBLISH_EVENT == updateStatus) {
            if (event.getEventState() == EventState.PENDING) {
                event.setEventState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                throw new ConflictException("Можно опубликовать только событие в состании ожидания (PENDING)");
            }
        }
        if (EventAdminStateAction.REJECT_EVENT == updateStatus) {
            if (!(event.getEventState() == EventState.PUBLISHED)) {
                event.setEventState(EventState.CANCELED);
            } else {
                throw new ConflictException("Отклонить событие можно только если оно не опубликовано (не PUBLISHED)");
            }
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

    private void validationSizeString(String annotation, String description, String title) {
        if (annotation != null) {
            if (annotation.length() < 20) {
                throw new ValidationException("Слишком короткий заголовок, min = 20");
            }
            if (annotation.length() > 2000) {
                throw new ValidationException("Слишком длинный заголовок, max = 2000");
            }
        }
        if (description != null) {
            if (description.length() < 20) {
                throw new ValidationException("Слишком короткое описание, min = 20");
            }
            if (description.length() > 7000) {
                throw new ValidationException("Слишком длинное описание, max = 7000");
            }
        }
        if (title != null) {
            if (title.length() < 3) {
                throw new ValidationException("Слишком короткий заголовок, min = 3");
            }
            if (title.length() > 120) {
                throw new ValidationException("Слишком длинный заголовок, max = 120");
            }
        }
    }
}
