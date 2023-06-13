package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private List<Integer> events;
    private Boolean pinned;
    @NotBlank(message = "Название не должно быть пустым")
    private String title;
}
