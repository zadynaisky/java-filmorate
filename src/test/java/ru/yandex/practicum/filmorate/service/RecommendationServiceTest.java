package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilmRepository filmRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    public void testGetRecommendations_UserNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(null);

        // When & Then
        assertThrows(NotFoundException.class, () -> recommendationService.getRecommendations(userId));
    }

    @Test
    public void testGetRecommendations_UserHasNoLikes() {
        // Given
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(user);
        when(recommendationRepository.getUserLikedFilms(userId)).thenReturn(Collections.emptySet());

        // When
        List<Film> recommendations = recommendationService.getRecommendations(userId);

        // Then
        assertTrue(recommendations.isEmpty());
    }

    @Test
    public void testGetRecommendations_Success() {
        // Given
        Long userId = 1L;
        Long similarUserId = 2L;
        User user = new User();
        Set<Long> userLikes = Set.of(1L, 2L);
        List<Long> recommendedFilmIds = Arrays.asList(17L); // берем id фильма, который ожидается в API

        Film film = new Film();
        film.setId(17L);
        film.setName("LnDqYQAji6rxRhA");
        film.setDescription("HK9tzKGFqk1K7L5XsMHK5yZpmSNvr5lAIsDcCIlilFo0etBhU3");
        film.setReleaseDate(LocalDate.of(1962, 6, 4));
        film.setDuration(108);

        when(userRepository.findById(userId)).thenReturn(user);
        when(recommendationRepository.getUserLikedFilms(userId)).thenReturn(userLikes);
        when(recommendationRepository.findUserWithMostCommonLikes(userId)).thenReturn(similarUserId);
        when(recommendationRepository.getRecommendedFilmIds(userId, similarUserId)).thenReturn(recommendedFilmIds);
        when(filmRepository.findById(17L)).thenReturn(film);

        // When
        List<Film> recommendations = recommendationService.getRecommendations(userId);

        // Then
        assertEquals(1, recommendations.size());
        assertEquals(film, recommendations.get(0));
    }

    @Test
    public void testGetRecommendations_NoSimilarUsers() {
        // Given
        Long userId = 1L;
        User user = new User();
        Set<Long> userLikes = Set.of(1L, 2L);

        when(userRepository.findById(userId)).thenReturn(user);
        when(recommendationRepository.getUserLikedFilms(userId)).thenReturn(userLikes);
        when(recommendationRepository.findUserWithMostCommonLikes(userId)).thenReturn(null);

        // When
        List<Film> recommendations = recommendationService.getRecommendations(userId);

        // Then
        assertTrue(recommendations.isEmpty());
    }
}