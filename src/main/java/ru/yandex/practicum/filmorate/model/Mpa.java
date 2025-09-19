package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Mpa {
    @NotNull
    private long id;
    private String name;
    private String description;
}