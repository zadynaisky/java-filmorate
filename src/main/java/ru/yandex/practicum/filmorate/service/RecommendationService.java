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
        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Set<Long> userLikedFilms = recommendationRepository.getUserLikedFilms(userId);

        if (userLikedFilms.isEmpty()) {
            List<Long> allFilms = recommendationRepository.getAllFilmsNotLikedByUser(userId);
            if (allFilms.size() > 10) {
                allFilms = allFilms.subList(0, 10);
            }
            return convertFilmIdsToFilms(allFilms);
        }

        // Находим пользователя с максимальным количеством общих лайков
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);

        if (similarUserId == null) {
            return Collections.emptyList();
        }

        // Получаем рекомендации от похожего пользователя
        List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);

        if (recommendedFilmIds.size() > 10) {
            recommendedFilmIds = recommendedFilmIds.subList(0, 10);
        }

        return convertFilmIdsToFilms(recommendedFilmIds);
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