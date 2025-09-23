package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Film implements Comparable<Film> {

    @EqualsAndHashCode.Include
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

    @NotNull
    @Valid
    private Mpa mpa;

    @NotNull
    @Valid
    private Set<Genre> genres = new LinkedHashSet<>(); // порядок + без дублей

    // защищаемся от null и копируем входной набор
    public void setGenres(Set<Genre> genres) {
        this.genres = (genres == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(genres);
    }

    @Override
    public int compareTo(Film o) {
        return Comparator.nullsFirst(Long::compare).compare(this.id, o.id);
    }
}
