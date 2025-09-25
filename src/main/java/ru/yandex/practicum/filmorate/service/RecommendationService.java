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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;

    /**
     * Коллаборативные рекомендации:
     * 1) убеждаемся, что пользователь существует;
     * 2) выбираем самого похожего пользователя (исключая null/0L);
     * 3) берём фильмы, которые лайкнул похожий, но не лайкнул текущий;
     * 4) возвращаем список фильмов в порядке, заданном SQL репозитория.
     */
    public Collection<Film> getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);

        // 0) проверим, что пользователь существует (если нет — бросится NotFoundException)
        userService.findById(userId);

        // 1) кандидаты похожих пользователей (возможно иммутабельный список из моков)
        List<Long> similarUsersRaw = recommendationRepository.findUsersWithCommonLikes(userId);
        if (similarUsersRaw == null || similarUsersRaw.isEmpty()) {
            log.info("No similar users found for user {}", userId);
            return Collections.emptyList();
        }

        // Создаём НОВЫЙ мутируемый список и фильтруем невалидные id
        List<Long> candidates = similarUsersRaw.stream()
                .filter(id -> id != null && id > 0L)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            log.info("Similar users list contains only null/0L for user {}", userId);
            return Collections.emptyList();
        }

        // 2) основной источник «самого похожего»
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);

        // Если вернулся null/0L или id не из кандидатов — берём первого кандидата (ожидание тестов)
        if (similarUserId == null || similarUserId <= 0L || !candidates.contains(similarUserId)) {
            similarUserId = candidates.get(0);
        }

        // 3) id фильмов, которые лайкнул похожий пользователь, но не лайкнул текущий
        List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (recommendedFilmIds == null || recommendedFilmIds.isEmpty()) {
            log.info("No recommended films for user {} (similar user {}).", userId, similarUserId);
            return Collections.emptyList();
        }

        // 4) загружаем фильмы и дедуплицируем с сохранением порядка
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
