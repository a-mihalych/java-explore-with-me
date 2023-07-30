package ru.practicum.rating.mapper;

import ru.practicum.event.dto.EventShortDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.model.Rating;
import ru.practicum.user.dto.UserShortDto;

public class RatingMapper {

    public static RatingDto toRatingDto(Rating rating, UserShortDto userShortDto, EventShortDto eventShortDto) {
        return RatingDto.builder()
                        .id(rating.getId())
                        .user(userShortDto)
                        .event(eventShortDto)
                        .status(rating.getStatus())
                        .build();
    }
}
