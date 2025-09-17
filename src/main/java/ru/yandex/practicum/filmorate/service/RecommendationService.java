package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.*;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public RecommendationService(RecommendationRepository recommendationRepository,
                                UserRepository userRepository,
                                FilmRepository filmRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    public List<Film> getRecommendations(Long userId) {
        try {
            // Проверяем существование пользователя
            if (userRepository.findById(userId) == null) {
                throw new NotFoundException("User with id " + userId + " not found");
            }

            // Получаем фильмы, которые уже лайкнул текущий пользователь
            Set<Long> userLikedFilms = recommendationRepository.getUserLikedFilms(userId);

            // Если пользователь не лайкнул ни одного фильма, возвращаем пустой список
            if (userLikedFilms.isEmpty()) {
                return Collections.emptyList();
            }

            // Простой алгоритм: найти любого другого пользователя и взять его лайки, которых нет у текущего
            Set<Long> allUsers = recommendationRepository.getAllUsersWithLikes();

            for (Long otherUserId : allUsers) {
                if (!otherUserId.equals(userId)) {
                    Set<Long> otherUserLikes = recommendationRepository.getUserLikedFilms(otherUserId);

                    // Находим фильмы, которые лайкнул другой пользователь, но не лайкнул текущий
                    List<Long> recommendations = new ArrayList<>();
                    for (Long filmId : otherUserLikes) {
                        if (!userLikedFilms.contains(filmId)) {
                            recommendations.add(filmId);
                            // Ограничиваем количество рекомендаций
                            if (recommendations.size() >= 10) {
                                break;
                            }
                        }
                    }

                    if (!recommendations.isEmpty()) {
                        return convertFilmIdsToFilms(recommendations);
                    }
                }
            }

            // Если ничего не нашли, возвращаем пустой список
            return Collections.emptyList();

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            // В случае любой ошибки возвращаем пустой список
            return Collections.emptyList();
        }
    }

    private List<Film> convertFilmIdsToFilms(List<Long> filmIds) {
        List<Film> films = new ArrayList<>();
        for (Long filmId : filmIds) {
            try {
                Film film = filmRepository.findById(filmId);
                if (film != null) {
                    films.add(film);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return films;
    }
}