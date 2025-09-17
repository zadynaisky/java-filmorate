package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

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
        log.info("Getting recommendations for user {}", userId);

        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Set<Long> userLikedFilms = recommendationRepository.getUserLikedFilms(userId);
        log.info("User {} has {} liked films: {}", userId, userLikedFilms.size(), userLikedFilms);

        // 1. Если у пользователя нет лайков, возвращаем популярные фильмы
        if (userLikedFilms.isEmpty()) {
            log.info("User {} has no likes, returning popular films", userId);
            return getPopularFilms(10); // Возвращаем топ-10 популярных фильмов
        }

        // 2. Находим пользователя с максимальным количеством общих лайков
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        log.info("Found similar user {} for user {}", similarUserId, userId);

        List<Long> recommendedFilmIds = new ArrayList<>();

        if (similarUserId != null) {
            // 3. Берём фильмы, которые лайкал похожий пользователь, но не лайкал текущий
            recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
            log.info("Found {} recommended film IDs from similar user for user {}: {}",
                    recommendedFilmIds.size(), userId, recommendedFilmIds);
        }

        // 4. Если не нашли рекомендаций от похожего пользователя,
        //    ищем любые фильмы, которые пользователь еще не лайкал
        if (recommendedFilmIds.isEmpty()) {
            log.info("No recommendations from similar user, finding any unliked films for user {}", userId);
            recommendedFilmIds = recommendationRepository.getAnyUnlikedFilmIds(userId, userLikedFilms, 10);
            log.info("Found {} unliked film IDs for user {}: {}",
                    recommendedFilmIds.size(), userId, recommendedFilmIds);
        }

        List<Film> result = convertFilmIdsToFilms(recommendedFilmIds);
        log.info("Returning {} recommendations for user {}", result.size(), userId);
        return result;
    }

    private List<Film> getPopularFilms(int count) {
        try {
            // Используем метод из FilmRepository для получения популярных фильмов
            Collection<Film> popularFilms = filmRepository.getTop(count);
            return new ArrayList<>(popularFilms);
        } catch (Exception e) {
            log.error("Error getting popular films: {}", e.getMessage());
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
                } else {
                    log.warn("Film with id {} not found", filmId);
                }
            } catch (Exception e) {
                log.warn("Error retrieving film with id {}: {}", filmId, e.getMessage());
            }
        }
        return films;
    }
}