package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;

    /**
     * Рекомендации по коллаборативной фильтрации:
     * 1) проверяем, что пользователь существует;
     * 2) берём его лайки;
     * 3) находим самого похожего пользователя;
     * 4) берём фильмы, которые лайкнул похожий, но не лайкнул текущий;
     * 5) загружаем сущности Film, убирая дубли и null (с сохранением порядка).
     */
    public Collection<Film> getRecommendations(Long userId) {
        // 1) проверка существования пользователя (бросит NotFoundException, если нет)
        userService.findById(userId);

        // 2) лайки текущего пользователя
        Set<Long> userLiked = recommendationRepository.getUserLikedFilms(userId);
        if (userLiked == null || userLiked.isEmpty()) {
            return List.of();
        }

        // 3) наиболее похожий пользователь
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        if (similarUserId == null) {
            return List.of();
        }

        // 4) id фильмов, которые лайкнул похожий пользователь и не лайкнул текущий
        List<Long> ids = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // 5) загрузка фильмов, дедупликация с сохранением порядка
        Map<Long, Film> unique = new LinkedHashMap<>();
        for (Long id : ids) {
            Film f = filmRepository.findById(id);
            if (f != null) {
                unique.putIfAbsent(f.getId(), f);
            }
        }
        return new ArrayList<>(unique.values());
    }
}
