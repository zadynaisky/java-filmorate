package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    /**
     * Рекомендации по коллаборативной фильтрации:
     * 1) находим пользователя с максимальным пересечением лайков,
     * 2) берём его лайкнутые фильмы, вычитаем фильмы текущего,
     * 3) возвращаем уникальные фильмы (в исходном порядке).
     */
    public List<Film> getRecommendations(Long userId) {
        // 1) проверяем, что пользователь существует
        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        // 2) лайки текущего пользователя
        Set<Long> userLiked = recommendationRepository.getUserLikedFilms(userId);
        if (userLiked == null || userLiked.isEmpty()) {
            return Collections.emptyList();
        }

        // 3) находим наиболее похожего пользователя
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        if (similarUserId == null) {
            return Collections.emptyList();
        }

        // 4) id фильмов, которые лайкнул похожий пользователь и не лайкнул текущий
        List<Long> recommendedIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (recommendedIds == null || recommendedIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 5) загружаем фильмы, убирая дубли и null, сохраняя порядок
        List<Film> result = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>(recommendedIds); // сохраняет порядок и удаляет дубли
        for (Long id : seen) {
            Film film = filmRepository.findById(id);
            if (film != null) {
                result.add(film);
            }
        }
        return result;
    }
}
