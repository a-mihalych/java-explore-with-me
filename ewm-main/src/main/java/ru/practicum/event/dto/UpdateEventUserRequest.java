package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.locate.dto.Location;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {

    // todo два похожих один удалить

    private String annotation;
    private Integer category;
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String stateAction;
    private String title;
}
