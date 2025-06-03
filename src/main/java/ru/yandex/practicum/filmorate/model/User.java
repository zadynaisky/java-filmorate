package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    @Email(message = "incorrect email")
    private String email;
    @NotBlank(message = "login cannot be null or empty")
    @Pattern(regexp = "^\\S+$", message = "login must not contain whitespace characters")
    private String login;
    private String name;
    @Past(message = "birthday must be a date in the past")
    private LocalDate birthday;
}
