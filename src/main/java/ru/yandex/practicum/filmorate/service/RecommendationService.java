package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final FilmRepository filmRepository;

    public RecommendationService(RecommendationRepository recommendationRepository, UserRepository userRepository, FilmRepository filmRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    public List<Film> getRecommendations(Long userId) {
        // Проверяем существование пользователя
        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        // Получаем фильмы, которые уже лайкнул текущий пользователь
        Set<Long> userLikedFilms = recommendationRepository.getUserLikedFilms(userId);
        
        // Если пользователь не лайкнул ни одного фильма, возвращаем пустой список
        if (userLikedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        // Находим пользователя с максимальным количеством общих лайков
        Long similarUserId = findMostSimilarUser(userId);
        if (similarUserId == null) {
            log.info("No similar users for {} — empty recommendations.", userId);
            return Collections.emptyList();
        }

        // Получаем ID рекомендованных фильмов
        List<Long> recommendedFilmIds = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        
        // Преобразуем ID в объекты Film
        return recommendedFilmIds.stream()
                .map(filmRepository::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long findMostSimilarUser(Long userId) {
        return recommendationRepository.findUserWithMostCommonLikes(userId);
    }

}
