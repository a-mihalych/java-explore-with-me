package ru.practicum.compilation.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    private List<Integer> events;
    private Boolean pinned;
    private String title;
}
