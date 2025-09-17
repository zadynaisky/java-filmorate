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

            // Если пользователь не лайкнул ни одного фильма → возвращаем любые нелайкнутые фильмы
            if (userLikedFilms.isEmpty()) {
                List<Long> allUnlikedFilms = recommendationRepository.getAllFilmsNotLikedByUser(userId);
                return convertFilmIdsToFilms(allUnlikedFilms);
            }

            List<Film> recommendations = new ArrayList<>();

            // Стратегия 1: Ищем пользователя с общими лайками
            Long similarUserId = findMostSimilarUser(userId);
            if (similarUserId != null) {
                List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
                recommendations = convertFilmIdsToFilms(recommendedFilmIds);
                if (!recommendations.isEmpty()) {
                    return recommendations;
                }
            }

            // Стратегия 2: Ищем любого другого пользователя с лайками
            Set<Long> allUsersWithLikes = recommendationRepository.getAllUsersWithLikes();
            for (Long otherUserId : allUsersWithLikes) {
                if (!otherUserId.equals(userId)) {
                    List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, otherUserId);
                    recommendations = convertFilmIdsToFilms(recommendedFilmIds);
                    if (!recommendations.isEmpty()) {
                        return recommendations;
                    }
                }
            }

            // Стратегия 3: Берем любые фильмы, которые не лайкнул пользователь
            List<Long> allUnlikedFilms = recommendationRepository.getAllFilmsNotLikedByUser(userId);
            recommendations = convertFilmIdsToFilms(allUnlikedFilms);

            return recommendations;

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

    private Long findMostSimilarUser(Long userId) {
        try {
            return recommendationRepository.findUserWithMostCommonLikes(userId);
        } catch (Exception e) {
            return null;
        }
    }
}