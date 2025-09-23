package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public RecommendationService(RecommendationRepository recommendationRepository, UserRepository userRepository, FilmRepository filmRepository) {
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
        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Set<Long> userLikedFilms = recommendationRepository.getUserLikedFilms(userId);

        if (userLikedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        Long similarUserId = findMostSimilarUser(userId);
        if (similarUserId == null) {
            return Collections.emptyList();
        }

            // Получаем ID рекомендованных фильмов
            List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);

        return recommendedFilmIds.stream()
                .map(filmRepository::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long findMostSimilarUser(Long userId) {
        return recommendationRepository.findUserWithMostCommonLikes(userId);
    }

}
