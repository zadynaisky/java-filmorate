package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User implements Comparable<User> {
    private Long id;
    @Email(message = "incorrect email")
    private String email;
    @NotBlank(message = "login cannot be null or empty")
    @Pattern(regexp = "^\\S+$", message = "login must not contain whitespace characters")
    private String login;
    private String name;
    @Past(message = "birthday must be a date in the past")
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();

    @Override
    public int compareTo(User o) {
        return Long.compare(this.id, o.id);
    }
}
