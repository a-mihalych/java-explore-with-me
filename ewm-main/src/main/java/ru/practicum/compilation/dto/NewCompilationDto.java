package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    @Builder.Default
    private List<Integer> events = List.of();
    @Builder.Default
    private Boolean pinned = false;
    @NotBlank(message = "Название не должно быть пустым")
    @Size(min = 1, max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    private String title;
}
