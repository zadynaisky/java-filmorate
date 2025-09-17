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
import ru.yandex.practicum.filmorate.storage.repository.LikeRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private LikeRepository likeRepository;

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
        Map<Long, Set<Long>> allUsersLikes = new HashMap<>();
        allUsersLikes.put(1L, Set.of(1L, 2L)); // user1 likes films 1, 2
        allUsersLikes.put(2L, Set.of(1L, 2L, 3L)); // user2 likes films 1, 2, 3 (most similar)

        when(userService.findById(userId)).thenReturn(user1);
        when(likeRepository.getAllUsersLikes()).thenReturn(allUsersLikes);
        when(filmRepository.findById(3L)).thenReturn(film3);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertEquals(1, recommendations.size());
        assertTrue(recommendations.contains(film3));
        verify(userService).findById(userId);
        verify(likeRepository).getAllUsersLikes();
        verify(filmRepository).findById(3L);
    }

    @Test
    void getRecommendations_ShouldReturnEmptyList_WhenUserHasNoLikes() {
        // Arrange
        Long userId = 1L;
        Map<Long, Set<Long>> allUsersLikes = new HashMap<>();
        allUsersLikes.put(2L, Set.of(1L, 2L));

        when(userService.findById(userId)).thenReturn(user1);
        when(likeRepository.getAllUsersLikes()).thenReturn(allUsersLikes);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertTrue(recommendations.isEmpty());
        verify(userService).findById(userId);
        verify(likeRepository).getAllUsersLikes();
        verify(filmRepository, never()).findById(any());
    }

    @Test
    void getRecommendations_ShouldReturnEmptyList_WhenNoSimilarUsersFound() {
        // Arrange
        Long userId = 1L;
        Map<Long, Set<Long>> allUsersLikes = new HashMap<>();
        allUsersLikes.put(1L, Set.of(1L, 2L)); // user1 likes films 1, 2
        allUsersLikes.put(2L, Set.of(3L, 4L)); // user2 likes films 3, 4 (no intersection)

        when(userService.findById(userId)).thenReturn(user1);
        when(likeRepository.getAllUsersLikes()).thenReturn(allUsersLikes);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertTrue(recommendations.isEmpty());
        verify(userService).findById(userId);
        verify(likeRepository).getAllUsersLikes();
        verify(filmRepository, never()).findById(any());
    }

    @Test
    void getRecommendations_ShouldReturnMultipleFilms_WhenSimilarUserLikesMultipleNewFilms() {
        // Arrange
        Long userId = 1L;
        Map<Long, Set<Long>> allUsersLikes = new HashMap<>();
        allUsersLikes.put(1L, Set.of(1L)); // user1 likes film 1
        allUsersLikes.put(2L, Set.of(1L, 2L, 3L)); // user2 likes films 1, 2, 3

        when(userService.findById(userId)).thenReturn(user1);
        when(likeRepository.getAllUsersLikes()).thenReturn(allUsersLikes);
        when(filmRepository.findById(2L)).thenReturn(film2);
        when(filmRepository.findById(3L)).thenReturn(film3);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertEquals(2, recommendations.size());
        assertTrue(recommendations.contains(film2));
        assertTrue(recommendations.contains(film3));
        verify(userService).findById(userId);
        verify(likeRepository).getAllUsersLikes();
        verify(filmRepository).findById(2L);
        verify(filmRepository).findById(3L);
    }

    @Test
    void getRecommendations_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenThrow(new NotFoundException("User not found"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> recommendationService.getRecommendations(userId));
        verify(userService).findById(userId);
        verify(likeRepository, never()).getAllUsersLikes();
    }

    @Test
    void getRecommendations_ShouldChooseMostSimilarUser_WhenMultipleSimilarUsersExist() {
        // Arrange
        Long userId = 1L;
        Map<Long, Set<Long>> allUsersLikes = new HashMap<>();
        allUsersLikes.put(1L, Set.of(1L, 2L, 3L)); // user1 likes films 1, 2, 3
        allUsersLikes.put(2L, Set.of(1L, 4L)); // user2 likes films 1, 4 (1 intersection)
        allUsersLikes.put(3L, Set.of(1L, 2L, 5L)); // user3 likes films 1, 2, 5 (2 intersections - most similar)

        Film film5 = new Film();
        film5.setId(5L);
        film5.setName("Film 5");

        when(userService.findById(userId)).thenReturn(user1);
        when(likeRepository.getAllUsersLikes()).thenReturn(allUsersLikes);
        when(filmRepository.findById(5L)).thenReturn(film5);

        // Act
        Collection<Film> recommendations = recommendationService.getRecommendations(userId);

        // Assert
        assertEquals(1, recommendations.size());
        assertTrue(recommendations.contains(film5)); // Should recommend film 5 from user3, not film 4 from user2
        verify(userService).findById(userId);
        verify(likeRepository).getAllUsersLikes();
        verify(filmRepository).findById(5L);
        verify(filmRepository, never()).findById(4L);
    }
}