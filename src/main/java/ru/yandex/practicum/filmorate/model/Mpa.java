package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mpa {
    @NotNull
    @EqualsAndHashCode.Include
    private long id;

    private String name;
    private String description;
}
