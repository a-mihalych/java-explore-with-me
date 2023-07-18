package ru.practicum.user.dto;

import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    private Integer id;
    private String name;
}
