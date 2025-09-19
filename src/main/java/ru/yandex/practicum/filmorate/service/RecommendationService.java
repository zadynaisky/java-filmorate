package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Рекомендации на основе коллаборативной фильтрации.
 * Вся тяжёлая работа делается SQL'ем в RecommendationRepository.
 */
@Service
public class RecommendationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RecommendationService.class);

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;

    public RecommendationService(RecommendationRepository recommendationRepository,
                                 FilmRepository filmRepository,
                                 UserService userService) {
        this.recommendationRepository = recommendationRepository;
        this.filmRepository = filmRepository;
        this.userService = userService;
    }

    public Collection<Film> getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);

        // убедимся, что пользователь существует
        userService.findById(userId);

        // 1) находим "похожих" пользователей (отсортированы по числу общих лайков)
        List<Long> similarUsers = recommendationRepository.findUsersWithCommonLikes(userId);
        if (similarUsers.isEmpty()) {
            log.info("No similar users found for user {}", userId);
            return Collections.emptyList();
        }

        Long mostSimilarUserId = similarUsers.get(0);

        // 2) фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий
        List<Long> recommendedFilmIds =
                recommendationRepository.getRecommendedFilmIds(userId, mostSimilarUserId);

        if (recommendedFilmIds.isEmpty()) {
            log.info("No recommended films found for user {} based on similar user {}", userId, mostSimilarUserId);
            return Collections.emptyList();
        }

        // 3) загружаем сущности фильмов
        return recommendedFilmIds.stream()
                .map(filmRepository::findById)
                .collect(Collectors.toList());
    }
}
