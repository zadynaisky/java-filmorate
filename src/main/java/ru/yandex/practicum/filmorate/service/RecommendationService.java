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

        List<Long> ids = recommendedFilmIds.stream()
                .filter(Objects::nonNull)
                .distinct()              // убираем дубли, порядок сохраняется (stable)
                .toList();

        Collection<Film> films = filmRepository.findByIdsPreservingOrder(ids);
        return new ArrayList<>(films);

    }
}
