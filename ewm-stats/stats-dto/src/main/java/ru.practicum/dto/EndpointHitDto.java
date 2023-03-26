package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

    @NotBlank(message = "Поле 'app' не должно быть пустым")
    private String app;
    @NotBlank(message = "Поле 'uri' не должно быть пустым")
    private String uri;
    @NotBlank(message = "Поле 'ip' не должно быть пустым")
    private String ip;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
