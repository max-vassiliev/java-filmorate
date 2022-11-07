package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private int id;

    @NotBlank(message = "Необходимо указать адрес электронной почты")
    @Email(message = "Проверьте написание адреса электронной почты")
    private String email;

    @NotBlank(message = "Необходимо указать логин")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
