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
    private final UserService userService;

    public Collection<Film> getRecommendations(Long userId) {
        // проверим, что пользователь существует (бросит NotFoundException при отсутствии)
        userService.findById(userId);

        // лайки текущего пользователя
        Set<Long> userLiked = recommendationRepository.getUserLikedFilms(userId);
        if (userLiked == null || userLiked.isEmpty()) {
            return Collections.emptyList();
        }

        // наиболее похожий пользователь по пересечению лайков
        Long similarUserId = recommendationRepository.findUserWithMostCommonLikes(userId);
        if (similarUserId == null) {
            return Collections.emptyList();
        }

        // id фильмов, которые лайкнул похожий пользователь и не лайкнул текущий
        List<Long> ids = recommendationRepository.getRecommendedFilmIds(userId, similarUserId);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // грузим фильмы, убираем дубли и null, сохраняем порядок
        Map<Long, Film> unique = new LinkedHashMap<>();
        for (Long id : ids) {
            Film f = filmRepository.findById(id);
            if (f != null) unique.putIfAbsent(f.getId(), f);
        }
        return new ArrayList<>(unique.values());
    }
}
