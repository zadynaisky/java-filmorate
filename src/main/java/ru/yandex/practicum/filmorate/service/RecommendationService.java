package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.LikeRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для рекомендации фильмов на основе коллаборативной фильтрации
 */
@Service
public class RecommendationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RecommendationService.class);
    
    private final LikeRepository likeRepository;
    private final FilmRepository filmRepository;
    private final UserService userService;
    
    public RecommendationService(LikeRepository likeRepository, FilmRepository filmRepository, UserService userService) {
        this.likeRepository = likeRepository;
        this.filmRepository = filmRepository;
        this.userService = userService;
    }

    /**
     * Получить рекомендации фильмов для пользователя
     * 
     * Алгоритм:
     * 1. Найти пользователей с максимальным количеством пересечений по лайкам
     * 2. Определить фильмы, которые один пролайкал, а другой нет
     * 3. Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами
     * 
     * @param userId ID пользователя, для которого составляются рекомендации
     * @return список рекомендованных фильмов
     */
    public Collection<Film> getRecommendations(Long userId) {
        log.info("Generating recommendations for user {}", userId);
        
        // Проверяем существование пользователя
        userService.findById(userId);
        
        // Получаем все лайки пользователей
        Map<Long, Set<Long>> allUsersLikes = likeRepository.getAllUsersLikes();
        
        // Получаем лайки целевого пользователя
        Set<Long> userLikes = allUsersLikes.getOrDefault(userId, new HashSet<>());
        
        if (userLikes.isEmpty()) {
            log.info("User {} has no likes, returning empty recommendations", userId);
            return Collections.emptyList();
        }
        
        // Находим пользователя с максимальным пересечением лайков
        Long mostSimilarUserId = findMostSimilarUser(userId, userLikes, allUsersLikes);
        
        if (mostSimilarUserId == null) {
            log.info("No similar users found for user {}", userId);
            return Collections.emptyList();
        }
        
        // Получаем рекомендации на основе лайков похожего пользователя
        Set<Long> similarUserLikes = allUsersLikes.get(mostSimilarUserId);
        Set<Long> recommendedFilmIds = new HashSet<>(similarUserLikes);
        recommendedFilmIds.removeAll(userLikes); // Убираем фильмы, которые пользователь уже лайкнул
        
        log.info("Found {} recommendations for user {} based on similar user {}", 
                recommendedFilmIds.size(), userId, mostSimilarUserId);
        
        // Конвертируем ID в объекты Film
        return recommendedFilmIds.stream()
                .map(filmRepository::findById)
                .collect(Collectors.toList());
    }
    
    /**
     * Найти пользователя с максимальным пересечением лайков
     */
    private Long findMostSimilarUser(Long userId, Set<Long> userLikes, Map<Long, Set<Long>> allUsersLikes) {
        Long mostSimilarUserId = null;
        int maxIntersection = 0;
        
        for (Map.Entry<Long, Set<Long>> entry : allUsersLikes.entrySet()) {
            Long otherUserId = entry.getKey();
            Set<Long> otherUserLikes = entry.getValue();
            
            // Пропускаем самого пользователя
            if (otherUserId.equals(userId)) {
                continue;
            }
            
            // Вычисляем пересечение лайков
            Set<Long> intersection = new HashSet<>(userLikes);
            intersection.retainAll(otherUserLikes);
            
            if (intersection.size() > maxIntersection) {
                maxIntersection = intersection.size();
                mostSimilarUserId = otherUserId;
            }
        }
        
        log.debug("Most similar user for {} is {} with {} common likes", 
                userId, mostSimilarUserId, maxIntersection);
        
        return mostSimilarUserId;
    }
}