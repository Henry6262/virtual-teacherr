package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.CourseRating;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.repositories.RatingRepository;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.RatingService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final CourseService courseService;


    @Override
    public CourseRating getById(int id) {
        return ratingRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Course Rating", "id", String.valueOf(id)));
    }

    @Override
    public List<CourseRating> getAllByCourseId(int courseId) {
        return ratingRepository.getAllByCourseId(courseId);
    }

    @Override
    public List<CourseRating> getAllByUserId(int userId) {
        return ratingRepository.getAllByUserId(userId);
    }

    public double getAverageRatingForCourse(Course course) {

        int numberOfRatings = getAllByCourseId(course.getId()).size();

        int allRatingsSum = getAllByCourseId(course.getId())
                .stream().mapToInt(CourseRating::getRating)
                .sum();

        return 1.0 * allRatingsSum / numberOfRatings;
    }

    public void create(Course course, User loggedUser, int rating) {

        if (getAllByUserId(loggedUser.getId())
                .stream()
                .map(CourseRating::getCourse)
                .anyMatch(c -> c.getId() == course.getId())) {

            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already rated Course with id: {%d}", loggedUser.getId(), course.getId()));
        }

        CourseRating courseRating = new CourseRating();
        courseRating.setCourse(course);
        courseRating.setUser(loggedUser);
        courseRating.setRating(rating);

        if (!loggedUser.hasCompletedCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has not completed Course with id: {%d}", courseRating.getUser().getId(), courseRating.getCourse().getId()));
        }

        ratingRepository.save(courseRating);
    }

}
