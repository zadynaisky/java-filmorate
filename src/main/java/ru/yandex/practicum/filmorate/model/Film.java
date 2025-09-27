package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
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

    // Разрешаем будущую дату: оставляем только кастомную проверку нижней границы
    @ReleaseDate
    private LocalDate releaseDate;

    @PositiveOrZero(message = "duration cannot be negative or zero")
    private int duration;

    @NotNull
    @Valid
    private Mpa mpa;

    @NotNull
    @Valid
    private Set<Genre> genres = new HashSet<>();

    @JsonProperty("directors")
    private Set<Director> directors = new LinkedHashSet<>();

    @Override
    public int compareTo(Film o) {
        // На случай null id: считаем null меньше ненулевого
        long thisId = (this.id == null) ? Long.MIN_VALUE : this.id;
        long otherId = (o == null || o.id == null) ? Long.MIN_VALUE : o.id;
        return Long.compare(thisId, otherId);
    }
}
