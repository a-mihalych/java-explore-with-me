package ru.practicum.rating.dto;

import lombok.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RatingEventDto {

    private UserShortDto user;
    private EventShortDto event;
}
