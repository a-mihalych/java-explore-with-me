package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.locate.dto.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "annotation - не должно быть пустым")
    @Size(min = 20, max = 2000)
    private String annotation;
    @NotBlank(message = "category - не должно быть пустым")
    @Positive
    private Integer category;
    @NotBlank(message = "description - не должно быть пустым")
    @Size(min = 20, max = 7000)
    private String description;
    @NotBlank(message = "eventDate - не должно быть пустым")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotBlank(message = "location - не должно быть пустым")
    private Location location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "title - не должно быть пустым")
    @Size(min = 3, max = 120)
    private String title;
}
