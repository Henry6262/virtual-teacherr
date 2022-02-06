package com.henrique.virtualteacher.services;

import com.henrique.virtualteacher.Helpers;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Rating;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.RatingRepository;
import com.henrique.virtualteacher.services.implementation.RatingServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTests {

    @Mock
    RatingRepository ratingRepository;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    RatingServiceImpl ratingService;


    @Test
    public void getById_shouldThrowException_whenEntityNotFound() {
        Rating rating = Helpers.createMockRating();
        Assertions.assertThrows(EntityNotFoundException.class, () -> ratingService.getById(rating.getId()));
    }

    @Test
    public void getAllByCourseId_shouldCallRepository() {
        Course course = Helpers.createMockCourse();
        List<Rating> ratings = Helpers.createMockRatingList();
        Mockito.when(ratingRepository.getAllByCourseId(course.getId())).thenReturn(ratings);

        ratingService.getAllByCourseId(course.getId());

        Mockito.verify(ratingRepository, Mockito.times(1))
                .getAllByCourseId(course.getId());
    }

    @Test
    public void getAllByUseId_shouldCallRepository() {
        User mockUser = Helpers.createMockUser();
        List<Rating> ratings = Helpers.createMockRatingList();

        Mockito.when(ratingRepository.getAllByUserId(mockUser.getId())).thenReturn(ratings);
        ratingService.getAllByUserId(mockUser.getId());
        Mockito.verify(ratingRepository, Mockito.times(1))
                .getAllByUserId(mockUser.getId());
    }

    @Test
    public void getAverageRatingForCourse_shouldReturn_correctNumber() {
        List<Rating> ratings = Helpers.createMockRatingList();
        ratings.get(0).setRating(4);
        ratings.get(1).setRating(3);
        Course mockCourse = Helpers.createMockCourse();
        mockCourse.setRatings(ratings);

        Mockito.when(ratingRepository.getAllByCourseId(Helpers.createMockCourse().getId())).thenReturn(ratings);

        Assertions.assertEquals(ratingService.getAverageRatingForCourse(mockCourse),4.4);
    }

    @Test
    public void create_shouldThrowException_whenUserAlreadyRatedCourse() {
        Course course = Helpers.createMockCourse();
        User mockUser = Helpers.createMockUser();
        List<Rating> ratings = Helpers.createMockRatingList();

        Mockito.when(ratingRepository.getAllByUserId(mockUser.getId())).thenReturn(ratings);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> ratingService.create(course, mockUser, 4));
    }

    @Test
    public void create_shouldThrowException_whenUserHasNotCompletedCourse() {
        Course course = Helpers.createMockCourse();
        course.setId(10);
        User mockUser = Helpers.createMockUser();
        List<Rating> ratings = Helpers.createMockRatingList();

        Mockito.when(ratingRepository.getAllByUserId(mockUser.getId())).thenReturn(ratings);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> ratingService.create(course, mockUser, 4));
    }

    @Test
    public void update_shouldThrowException_whenInitiatorIsNotCreator() {
        User initiator = Helpers.createMockUser();
        initiator.setId(12);
        Rating rating = Helpers.createMockRating();
        User creator = rating.getUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> ratingService.update(rating,2,initiator));
    }

    @Test
    public void delete_shouldThrowException_whenInitiatorIsNotCreator() {
        User initiator = Helpers.createMockUser();
        initiator.setId(12);
        Rating rating = Helpers.createMockRating();
        User creator = rating.getUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> ratingService.delete(rating, initiator));
    }



}
