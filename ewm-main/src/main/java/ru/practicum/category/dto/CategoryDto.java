package ru.practicum.category.dto;

import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Integer id;
    private String name;
}
