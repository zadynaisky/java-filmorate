package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;

import java.time.LocalDate;
import java.util.*;

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

    @NotNull @Valid
    private Mpa mpa;

    @NotNull @Valid
    private List<Genre> genres = new ArrayList<>();

    @Valid
    @JsonProperty("directors")
    private Set<Director> directors = new LinkedHashSet<>();

    @Override
    public int compareTo(Film o) {
        return Comparator.nullsFirst(Long::compare).compare(this.id, o.id);
    }
}
