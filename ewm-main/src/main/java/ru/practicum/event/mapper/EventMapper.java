package ru.practicum.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.locate.mapper.LocateMapper;
import ru.practicum.locate.model.Locate;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEvent(NewEventDto newEventDto, Category category, User user, Locate locate) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(user)
                .locate(locate)
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .publishedOn(null)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .eventState(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventFullDto toEventFullDto(Event event, Integer confirmedRequests,
                                              Integer views, Integer like, Integer dislike) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocateMapper.toLocation(event.getLocate()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getEventState().name())
                .title(event.getTitle())
                .views(views)
                .like(like)
                .dislike(dislike)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Integer confirmedRequests,
                                                Integer views, Integer like, Integer dislike) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .like(like)
                .dislike(dislike)
                .build();
    }

    public static Event toEvent(Event event, UpdateEventRequest updateEvent, Category category) {
        return event.toBuilder()
                .annotation(updateEvent.getAnnotation() != null ? updateEvent.getAnnotation() : event.getAnnotation())
                .category(category != null ? category : event.getCategory())
                .description(updateEvent.getDescription() != null ?
                        updateEvent.getDescription() : event.getDescription())
                .locate(updateEvent.getLocation() != null ?
                        LocateMapper.toLocate(updateEvent.getLocation()) : event.getLocate())
                .paid(updateEvent.getPaid() != null ? updateEvent.getPaid() : event.getPaid())
                .participantLimit(updateEvent.getParticipantLimit() != null ?
                        updateEvent.getParticipantLimit() : event.getParticipantLimit())
                .requestModeration(updateEvent.getRequestModeration() != null ?
                        updateEvent.getRequestModeration() : event.getRequestModeration())
                .title(updateEvent.getTitle() != null ? updateEvent.getTitle() : event.getTitle())
                .build();
    }
}
