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
     * 1) убеждаемся, что пользователь существует;
     * 2) забираем его лайки;
     * 3) берём самого похожего пользователя (или первого из отсортированного списка);
     * 4) берём фильмы, которые лайкнул похожий, но не лайкнул текущий;
     * 5) грузим сущности Film, убирая дубли и null, сохраняя порядок.
     */
    public Collection<Film> getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);

        // 1) проверка существования пользователя (бросит NotFoundException, если нет)
        userService.findById(userId);

        // 2) лайки текущего пользователя
        Set<Long> userLiked = recommendationRepository.getUserLikedFilms(userId);
        if (userLiked == null || userLiked.isEmpty()) {
            log.info("User {} has no likes — empty recommendations.", userId);
            return List.of();
        }

        // Вариант А: взять самого похожего по пересечению лайков
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);

        // Вариант Б (на случай, если метод выше вернул null): взять первого из отсортированного списка похожих
        if (similarUserId == null) {
            List<Long> similarUsers = recommendationRepository.findUsersWithCommonLikes(userId);
            if (similarUsers == null || similarUsers.isEmpty()) {
                log.info("No similar users for {} — empty recommendations.", userId);
                return List.of();
            }
            similarUserId = similarUsers.get(0);
        }

        // 4) id фильмов, которые лайкнул похожий пользователь и не лайкнул текущий
        List<Long> ids = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (ids == null || ids.isEmpty()) {
            log.info("No recommended films for user {} (similar user {}).", userId, similarUserId);
            return List.of();
        }

        // 5) загрузка фильмов + дедупликация с сохранением порядка
        Map<Long, Film> unique = new LinkedHashMap<>();
        for (Long id : ids) {
            Film f = filmRepository.findById(id);
            if (f != null) unique.putIfAbsent(f.getId(), f);
        }

        return new ArrayList<>(unique.values());
    }
}
