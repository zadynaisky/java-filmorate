package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

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
        log.info("Getting recommendations for user {}", userId);

        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Set<Long> userLikedFilms = recommendationRepository.getUserLikedFilms(userId);
        log.info("User {} has {} liked films: {}", userId, userLikedFilms.size(), userLikedFilms);

        // 1. Если у пользователя нет лайков, возвращаем пустой список
        if (userLikedFilms.isEmpty()) {
            log.info("User {} has no likes, returning empty list", userId);
            return Collections.emptyList();
        }

        List<Long> recommendedFilmIds = new ArrayList<>();

        // 2. Пытаемся найти рекомендации от похожих пользователей
        List<Long> similarUsers = recommendationRepository.findUsersWithCommonLikes(userId);
        log.info("Found {} similar users for user {}: {}", similarUsers.size(), userId, similarUsers);

        if (!similarUsers.isEmpty()) {
            // 3. Для каждого похожего пользователя получаем рекомендации
            for (Long similarUserId : similarUsers) {
                List<Long> filmsFromSimilarUser = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
                log.info("User {} recommended {} films from similar user {}: {}",
                        userId, filmsFromSimilarUser.size(), similarUserId, filmsFromSimilarUser);

                recommendedFilmIds.addAll(filmsFromSimilarUser);

                // Если нашли достаточно рекомендаций, выходим
                if (recommendedFilmIds.size() >= 10) {
                    break;
                }
            }
        }

        // 4. Если не нашли рекомендаций от похожих пользователей,
        //    ищем любые фильмы, которые пользователь еще не лайкал
        if (recommendedFilmIds.isEmpty()) {
            log.info("No recommendations from similar users, finding any unliked films for user {}", userId);
            recommendedFilmIds = recommendationRepository.findAnyUnlikedFilms(userId, 10);
            log.info("Found {} unliked films for user {}: {}",
                    recommendedFilmIds.size(), userId, recommendedFilmIds);
        }

        // 5. Убираем дубликаты и ограничиваем количество
        Set<Long> uniqueFilmIds = new LinkedHashSet<>(recommendedFilmIds);
        recommendedFilmIds = new ArrayList<>(uniqueFilmIds);

        if (recommendedFilmIds.size() > 10) {
            recommendedFilmIds = recommendedFilmIds.subList(0, 10);
        }

        log.info("Final recommended film IDs for user {}: {}", userId, recommendedFilmIds);

        List<Film> result = convertFilmIdsToFilms(recommendedFilmIds);
        log.info("Returning {} recommendations for user {}", result.size(), userId);
        return result;
    }

    private List<Film> convertFilmIdsToFilms(List<Long> filmIds) {
        List<Film> films = new ArrayList<>();
        for (Long filmId : filmIds) {
            try {
                Film film = filmRepository.findById(filmId);
                if (film != null) {
                    films.add(film);
                } else {
                    log.warn("Film with id {} not found", filmId);
                }
            } catch (Exception e) {
                log.warn("Error retrieving film with id {}: {}", filmId, e.getMessage());
            }
        }
        return films;
    }
}