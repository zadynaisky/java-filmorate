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

    /**
     * Получить рекомендации фильмов для пользователя
     *
     * Алгоритм:
     * 1. Найти пользователей с максимальным количеством пересечения по лайкам
     * 2. Определить фильмы, которые один пролайкал, а другой нет
     * 3. Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами
     */
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

            // Находим пользователя с максимальным количеством общих лайков
            Long similarUserId = findMostSimilarUser(userId);

            List<Long> recommendedFilmIds = new ArrayList<>();

            if (similarUserId != null) {
                // Получаем ID рекомендованных фильмов от похожего пользователя
                recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
            }

            // Если нет рекомендаций от похожего пользователя, берем любые фильмы, которые не лайкнул пользователь
            if (recommendedFilmIds.isEmpty()) {
                recommendedFilmIds = recommendationRepository.getAllFilmsNotLikedByUser(userId);
            }

            // Преобразуем ID в объекты Film
            List<Film> recommendations = new ArrayList<>();
            for (Long filmId : recommendedFilmIds) {
                try {
                    Film film = filmRepository.findById(filmId);
                    if (film != null) {
                        recommendations.add(film);
                    }
                } catch (Exception e) {
                    // Пропускаем фильмы, которые не удалось загрузить
                    continue;
                }
            }
            return recommendations;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            // В случае любой другой ошибки возвращаем пустой список
            return Collections.emptyList();
        }
    }

    /**
     * Найти пользователя с наибольшим количеством общих лайков
     */
    private Long findMostSimilarUser(Long userId) {
        try {
            return recommendationRepository.findUserWithMostCommonLikes(userId);
        } catch (Exception e) {
            return null;
        }
    }
}