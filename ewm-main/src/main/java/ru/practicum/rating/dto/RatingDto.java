package ru.practicum.rating.dto;

import lombok.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;

@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {

    private Integer id;
    private UserShortDto user;
    private EventShortDto event;
    private Boolean status;
}
