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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto, Category category, User user, Locate locate) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(user)
                .locate(locate)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .publishedOn(null)
                .requestModeration(newEventDto.getRequestModeration())
                .eventState(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventFullDto toEventFullDto(Event event, Integer confirmedRequests, Integer views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().toString())
                .description(event.getDescription())
                .eventDate(event.getEventDate().toString())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocateMapper.toLocation(event.getLocate()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .state(event.getEventState().name())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Integer confirmedRequests, Integer views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().toString())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
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
