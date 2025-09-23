package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;

    /**
     * Рекомендации по simple-collaborative принципу:
     *  - ищем похожего пользователя по пересечению лайков;
     *  - берём фильмы, которые лайкнул похожий, но не лайкнул текущий;
     *  - возвращаем список фильмов в порядке убывания «популярности» (закладывается в SQL репозитория).
     */
    public Collection<Film> getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);

        // 0) убедимся, что пользователь существует (бросит NotFoundException при отсутствии)
        userService.findById(userId);

        // 1) пользователи с общими лайками (отсортированы по количеству общих лайков)
        List<Long> similarUsers = recommendationRepository.findUsersWithCommonLikes(userId);
        if (similarUsers == null || similarUsers.isEmpty()) {
            log.info("No similar users found for user {}", userId);
            return Collections.emptyList();
        }

        // 2) лайки текущего пользователя — если нет лайков, коллаборативные рекомендации бессмысленны
        Set<Long> userLiked = recommendationRepository.getUserLikedFilms(userId);
        if (userLiked == null || userLiked.isEmpty()) {
            log.info("User {} has no likes — returning empty recommendations", userId);
            return Collections.emptyList();
        }

        // 3) находим самого похожего пользователя
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        if (similarUserId == null) {
            // fallback: первый из отсортированного списка
            similarUserId = similarUsers.get(0);
        }

        // 4) фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий (SQL уже сортирует по «популярности»)
        List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (recommendedFilmIds == null || recommendedFilmIds.isEmpty()) {
            log.info("No recommended films for user {} (similar user {}).", userId, similarUserId);
            return Collections.emptyList();
        }

        // 5) загружаем сущности фильмов, дедуплицируем с сохранением порядка
        Map<Long, Film> unique = new LinkedHashMap<>();
        for (Long id : recommendedFilmIds) {
            Film f = filmRepository.findById(id);
            if (f != null) {
                unique.putIfAbsent(f.getId(), f);
            }
        }

        return new ArrayList<>(unique.values());
    }
}
