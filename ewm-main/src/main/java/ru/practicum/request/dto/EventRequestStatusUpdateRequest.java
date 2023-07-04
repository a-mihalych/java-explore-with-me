package ru.practicum.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotNull(message = "requestIds - не должен быть null")
    private List<Integer> requestIds;
    @NotBlank(message = "status - не должен быть пустым")
    private String status;
}
