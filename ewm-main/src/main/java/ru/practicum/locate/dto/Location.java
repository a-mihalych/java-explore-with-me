package ru.practicum.locate.dto;

import lombok.*;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Float lat; // Широта
    private Float lon; // Долгота
}
