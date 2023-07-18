package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.rating.dto.NewRating;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventDto;
import ru.practicum.rating.mapper.RatingMapper;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stats.Stats;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final Stats stats;

    @Override
    @Transactional
    public RatingDto create(Integer userId, NewRating newRating) {
        User user = getUser(userId);
        UserShortDto userShortDto = UserMapper.toUserShortDto(user);
        Event event = getEvent(newRating.getEventId());
        EventShortDto eventShortDto = EventMapper.toEventShortDto(event,
                requestRepository.countRequestConfirmed(event.getId()),
                stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true));
        Rating rating = ratingRepository.findByUserIdAndEventId(userShortDto.getId(), event.getId());
        if (rating != null) {
            throw new ConflictException("Такой рейтинг уже существует");
        }
        return RatingMapper.toRatingDto(ratingRepository.save(Rating.builder()
                                                              .user(user)
                                                              .event(event)
                                                              .status(newRating.getStatus())
                                                              .build()),
                                                              userShortDto, eventShortDto);
    }

    @Override
    @Transactional
    public void delete(Integer userId, Integer ratingId) {
        getUser(userId);
        Rating rating = getRating(ratingId);
        if (!userId.equals(rating.getUser().getId())) {
            throw new ConflictException("Нельзя удалять чужой рейтинг");
        }
        ratingRepository.deleteById(ratingId);
    }

    @Override
    public RatingEventDto ratingEvent(Integer userId, Integer eventId) {
        User user = getUser(userId);
        UserShortDto userShortDto = UserMapper.toUserShortDto(user);
        Event event = getEvent(eventId);
        EventShortDto eventShortDto = EventMapper.toEventShortDto(event,
                requestRepository.countRequestConfirmed(event.getId()),
                stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                        List.of("/events/" + event.getId()), true));
        return RatingEventDto.builder()
                .user(userShortDto)
                .event(eventShortDto)
                .ratingTrue(ratingRepository.countRatingTrue(eventId))
                .ratingFalse(ratingRepository.countRatingFalse(eventId))
                .build();
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

    private Rating getRating(Integer ratingId) {
        return ratingRepository.findById(ratingId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден рейтинг с id = %d", ratingId));
        });
    }
}
