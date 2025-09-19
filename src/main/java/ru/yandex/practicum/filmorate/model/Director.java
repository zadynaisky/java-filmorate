package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    private Long id;

    @NotBlank(message = "Director name cannot be blank")
    private String name;
}