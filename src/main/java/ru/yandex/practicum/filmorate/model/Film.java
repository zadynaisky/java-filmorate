package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
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

    @NotNull @Valid
    private Mpa mpa;

    @NotNull @Valid
    private List<Genre> genres = new ArrayList<>();

    @Valid
    private Set<Genre> genres = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Mpa getMpa() {
        return mpa;
    }

    public void setMpa(Mpa mpa) {
        this.mpa = mpa;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    @Override
    public int compareTo(Film o) {
        return Comparator.nullsFirst(Long::compare).compare(this.id, o.id);
    }
}
