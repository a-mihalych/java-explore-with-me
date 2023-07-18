package ru.practicum.rating.service;

import ru.practicum.rating.dto.NewRating;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventDto;

public interface RatingService {

    RatingDto create(Integer userId, NewRating newRating);

    void delete(Integer userId, Integer ratingId);

    RatingEventDto ratingEvent(Integer userId, Integer eventId);
}
