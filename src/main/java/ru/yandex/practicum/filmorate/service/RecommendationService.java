// src/main/java/ru/yandex/practicum/filmorate/service/RecommendationService.java
package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;

    /**
     * Рекомендации фильмов пользователю по коллаборативной фильтрации:
     * 1) ищем пользователя с максимальным пересечением лайков,
     * 2) берём его лайкнутые фильмы и вычитаем те, что уже лайкнул целевой,
     * 3) возвращаем уникальные фильмы.
     */
    public Collection<Film> getRecommendations(long userId) {
        // проверяем, что пользователь существует (кинет NotFoundException, если нет)
        userService.findById(userId);

        // лайки текущего пользователя
        Set<Long> userLiked = recommendationRepository.getUserLikedFilms(userId);
        if (userLiked == null || userLiked.isEmpty()) {
            log.info("User {} has no likes — empty recommendations.", userId);
            return Collections.emptyList();
        }

        // находим наиболее похожего пользователя
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        if (similarUserId == null) {
            log.info("No similar users for {} — empty recommendations.", userId);
            return Collections.emptyList();
        }

        // id фильмов, которые лайкнул похожий пользователь и не лайкнул текущий
        List<Long> recommendedIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (recommendedIds == null || recommendedIds.isEmpty()) {
            return Collections.emptyList();
        }

        // загружаем фильмы и убираем возможные дубли, сохраняя порядок
        Map<Long, Film> unique = new LinkedHashMap<>();
        for (Long id : recommendedIds) {
            Film f = filmRepository.findById(id);
            if (f != null) {
                unique.putIfAbsent(f.getId(), f);
            }
        }
        return new ArrayList<>(unique.values());
    }
}
