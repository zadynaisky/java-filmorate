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

        // 1. Нет лайков → пустой список
        if (userLikedFilms.isEmpty()) {
            log.info("User {} has no likes, returning empty list", userId);
            return Collections.emptyList();
        }

        // 2. Находим пользователя с максимальным количеством общих лайков
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        log.info("Found similar user {} for user {}", similarUserId, userId);
        
        List<Long> recommendedFilmIds = new ArrayList<>();
        
        if (similarUserId != null) {
            // 3. Берём только те фильмы, которые лайкал похожий пользователь,
            //    но не лайкал текущий
            recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
            log.info("Found {} recommended film IDs from similar user for user {}: {}", recommendedFilmIds.size(), userId, recommendedFilmIds);
        } else {
            log.info("No similar users found, trying alternative approach for user {}", userId);
            // Альтернативный подход: найти любого пользователя с лайками и взять его фильмы
            Set<Long> allUsers = recommendationRepository.getAllUsersWithLikes();
            log.info("Found {} users with likes", allUsers.size());
            
            for (Long otherUserId : allUsers) {
                if (!otherUserId.equals(userId)) {
                    Set<Long> otherUserLikes = recommendationRepository.getUserLikedFilms(otherUserId);
                    log.info("User {} has {} likes: {}", otherUserId, otherUserLikes.size(), otherUserLikes);
                    
                    for (Long filmId : otherUserLikes) {
                        if (!userLikedFilms.contains(filmId)) {
                            recommendedFilmIds.add(filmId);
                        }
                    }
                    
                    if (!recommendedFilmIds.isEmpty()) {
                        log.info("Found {} recommendations from user {} for user {}", recommendedFilmIds.size(), otherUserId, userId);
                        break;
                    }
                }
            }
        }

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
                }
            } catch (Exception e) {
                // если фильм не найден, пропускаем
                continue;
            }
        }
        return films;
    }

}