package ru.practicum.category.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;
}
