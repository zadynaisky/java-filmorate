package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = {"id"})
public class Film {
    private Long id;
    @NotBlank(message = "name cannot be null or empty")
    private String name;
    @NotBlank(message = "description cannot be null or empty")
    @Size(max = 200, message = "description cannot be longer than 200 characters")
    private String description;
    @PastOrPresent(message = "releaseDate must be a date in the past or in the present")
    @ReleaseDate
    private LocalDate releaseDate;
    @Positive(message = "duration cannot be negative or zero")
    private int duration;
}
