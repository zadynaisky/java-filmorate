package ru.yandex.practicum.filmorate.model;

public enum SortBy {
    YEAR,
    LIKES;

    public static SortBy fromString(String value) {
        try {
            return value == null ? null : SortBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Sort must be 'year' or 'likes'");
        }
    }
}
