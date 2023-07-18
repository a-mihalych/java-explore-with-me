package ru.practicum.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 250, message = "Имя должно быть от 2 до 250 символов")
    private String name;
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email должен быть задан правильно")
    @Size(min = 6, max = 254, message = "Почта должна быть от 6 до 254 символов")
    private String email;
}
