package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;

    public Collection<Film> getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);

        // 0) проверим, что пользователь существует
        userService.findById(userId);

        // 1) пробуем определить "самого похожего" пользователя основным способом
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);

        // 2) fallback: если null/<=0 — попробуем взять первого из списка "похожих"
        if (similarUserId == null || similarUserId <= 0L) {
            List<Long> similarUsers = recommendationRepository.findUsersWithCommonLikes(userId);
            if (similarUsers != null && !similarUsers.isEmpty()) {
                // первый валидный id
                similarUserId = similarUsers.stream()
                        .filter(Objects::nonNull)
                        .filter(id -> id > 0L)
                        .findFirst()
                        .orElse(null);
            }
        }

        // если совсем никого не нашли — рекомендаций нет
        if (similarUserId == null || similarUserId <= 0L) {
            log.info("No similar users found for user {}", userId);
            return Collections.emptyList();
        }

        // 3) id фильмов, которые лайкнул похожий пользователь, но не лайкнул текущий
        List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (recommendedFilmIds == null || recommendedFilmIds.isEmpty()) {
            log.info("No recommended films for user {} (similar user {}).", userId, similarUserId);
            return Collections.emptyList();
        }

        // 4) грузим фильмы пакетно, убираем дубли, сохраняем порядок
        List<Long> ids = recommendedFilmIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Collection<Film> films = filmRepository.findByIdsPreservingOrder(ids);
        return new ArrayList<>(films);
    }
}
