package ru.practicum.rating.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewRating {

    @NotNull(message = "eventId - не должно быть пустым")
    @Positive
    private Integer eventId;
    @NotNull(message = "status - не должно быть пустым")
    private Boolean status;
}
