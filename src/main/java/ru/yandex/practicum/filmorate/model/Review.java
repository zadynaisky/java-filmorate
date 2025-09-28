package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Review {
    @JsonProperty("reviewId")
    private long id;
    @Size(max = 5000, message = "Content cannot be longer than 5000 characters")
    @NotBlank(message = "Content cannot be blank")
    private String content;
    @JsonProperty("isPositive")
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    private int useful;
}
