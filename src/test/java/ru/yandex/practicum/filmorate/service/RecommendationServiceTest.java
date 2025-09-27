package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.RecommendationRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RecommendationService recommendationService;

    private User user1;
    private User user2;
    private Film film1;
    private Film film2;
    private Film film3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");

        film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        film1.setMpa(mpa);

        film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(130);
        film2.setMpa(mpa);

        film3 = new Film();
        film3.setId(3L);
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2022, 1, 1));
        film3.setDuration(140);
        film3.setMpa(mpa);
    }

    @Test
    void getRecommendations_ShouldReturnRecommendedFilms_WhenSimilarUserExists() {
        // Arrange
        Long userId = 1L;
        List<Long> recommendedFilmIds = List.of(3L); // user2 likes film 3, but user1 doesn't

        when(userService.findById(userId)).thenReturn(user1);
        when(recommendationRepository.findUserWithMostCommonLikes(userId)).thenReturn(2L);
        when(recommendationRepository.getRecommendedFilmIds(userId, 2L)).thenReturn(recommendedFilmIds);
        when(filmRepository.findByIdsPreservingOrder(List.of(3L))).thenReturn(List.of(film3));

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertEquals(1, recommendations.size());
        assertTrue(recommendations.contains(film3));
        verify(userService).findById(userId);
        verify(recommendationRepository).findUserWithMostCommonLikes(userId);
        verify(recommendationRepository).getRecommendedFilmIds(userId, 2L);
        verify(filmRepository).findByIdsPreservingOrder(List.of(3L));
    }

    @Test
    void getRecommendations_ShouldReturnEmptyList_WhenUserHasNoLikes() {
        // Arrange
        Long userId = 1L;
        List<Long> similarUsers = Collections.emptyList(); // no similar users found

        when(userService.findById(userId)).thenReturn(user1);
        when(recommendationRepository.findUsersWithCommonLikes(userId)).thenReturn(similarUsers);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertTrue(recommendations.isEmpty());
        verify(userService).findById(userId);
        verify(recommendationRepository).findUsersWithCommonLikes(userId);
        verify(recommendationRepository, never()).getRecommendedFilmIds(any(), any());
        verify(filmRepository, never()).findById(any());
    }

    @Test
    void getRecommendations_ShouldReturnEmptyList_WhenNoSimilarUsersFound() {
        // Arrange
        Long userId = 1L;
        List<Long> similarUsers = Collections.emptyList(); // no similar users found

        when(userService.findById(userId)).thenReturn(user1);
        when(recommendationRepository.findUsersWithCommonLikes(userId)).thenReturn(similarUsers);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertTrue(recommendations.isEmpty());
        verify(userService).findById(userId);
        verify(recommendationRepository).findUsersWithCommonLikes(userId);
        verify(recommendationRepository, never()).getRecommendedFilmIds(any(), any());
        verify(filmRepository, never()).findById(any());
    }

    @Test
    void getRecommendations_ShouldReturnMultipleFilms_WhenSimilarUserLikesMultipleNewFilms() {
        // Arrange
        Long userId = 1L;
        List<Long> recommendedFilmIds = List.of(2L, 3L); // user2 likes films 2, 3, but user1 doesn't

        when(userService.findById(userId)).thenReturn(user1);
        when(recommendationRepository.findUserWithMostCommonLikes(userId)).thenReturn(2L);
        when(recommendationRepository.getRecommendedFilmIds(userId, 2L)).thenReturn(recommendedFilmIds);
        when(filmRepository.findByIdsPreservingOrder(List.of(2L, 3L))).thenReturn(List.of(film2, film3));

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertEquals(2, recommendations.size());
        assertTrue(recommendations.contains(film2));
        assertTrue(recommendations.contains(film3));
        verify(userService).findById(userId);
        verify(recommendationRepository).findUserWithMostCommonLikes(userId);
        verify(recommendationRepository).getRecommendedFilmIds(userId, 2L);
        verify(filmRepository).findByIdsPreservingOrder(List.of(2L, 3L));
    }

    @Test
    void getRecommendations_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenThrow(new NotFoundException("User not found"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> recommendationService.getRecommendations(userId));
        verify(userService).findById(userId);
        verify(recommendationRepository, never()).findUsersWithCommonLikes(any());
    }

    @Test
    void getRecommendations_ShouldChooseMostSimilarUser_WhenMultipleSimilarUsersExist() {
        // Arrange
        Long userId = 1L;
        List<Long> recommendedFilmIds = List.of(5L); // user3 likes film 5, but user1 doesn't

        Film film5 = new Film();
        film5.setId(5L);
        film5.setName("Film 5");

        when(userService.findById(userId)).thenReturn(user1);
        when(recommendationRepository.findUserWithMostCommonLikes(userId)).thenReturn(3L);
        when(recommendationRepository.getRecommendedFilmIds(userId, 3L)).thenReturn(recommendedFilmIds);
        when(filmRepository.findByIdsPreservingOrder(List.of(5L))).thenReturn(List.of(film5));

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertEquals(1, recommendations.size());
        assertTrue(recommendations.contains(film5)); // Should recommend film 5 from user3, not film 4 from user2
        verify(userService).findById(userId);
        verify(recommendationRepository).findUserWithMostCommonLikes(userId);
        verify(recommendationRepository).getRecommendedFilmIds(userId, 3L);
        verify(filmRepository).findByIdsPreservingOrder(List.of(5L));
        verify(filmRepository, never()).findByIdsPreservingOrder(List.of(4L));
    }
}