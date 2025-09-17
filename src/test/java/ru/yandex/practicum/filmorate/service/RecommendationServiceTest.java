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
        when(recommendationRepository.getAllFilmsNotLikedByUser(userId)).thenReturn(Arrays.asList(1L, 2L));

        Film film1 = new Film();
        Film film2 = new Film();
        when(filmRepository.findById(1L)).thenReturn(film1);
        when(filmRepository.findById(2L)).thenReturn(film2);

        // When
        List<Film> recommendations = recommendationService.getRecommendations(userId);

        // Then
        assertEquals(2, recommendations.size());
    }

    @Test
    public void testGetRecommendations_Success() {
        // Given
        Long userId = 1L;
        Long otherUserId = 2L;
        User user = new User();
        Set<Long> userLikes = Set.of(1L, 2L);
        Set<Long> otherUserLikes = Set.of(2L, 3L, 4L);
        Set<Long> allUsers = Set.of(1L, 2L);

        Film film1 = new Film();
        Film film2 = new Film();

        when(userRepository.findById(userId)).thenReturn(user);
        when(recommendationRepository.getUserLikedFilms(userId)).thenReturn(userLikes);
        when(recommendationRepository.getAllUsersWithLikes()).thenReturn(allUsers);
        when(recommendationRepository.getUserLikedFilms(otherUserId)).thenReturn(otherUserLikes);
        when(filmRepository.findById(3L)).thenReturn(film1);
        when(filmRepository.findById(4L)).thenReturn(film2);

        // When
        List<Film> recommendations = recommendationService.getRecommendations(userId);

        // Then
        assertEquals(2, recommendations.size());
        assertTrue(recommendations.contains(film1));
        assertTrue(recommendations.contains(film2));
    }
}