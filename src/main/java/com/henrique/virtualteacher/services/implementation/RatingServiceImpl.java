package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Rating;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.RatingRepository;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.RatingService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserService userService;

    @Override
    public Rating getById(int id) {
        return ratingRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Course Rating", "id", String.valueOf(id)));
    }

    @Override
    public List<Rating> getAllByCourseId(int courseId) {
        return ratingRepository.getAllByCourseId(courseId);
    }

    @Override
    public List<Rating> getAllByUserId(int userId) {
        return ratingRepository.getAllByUserId(userId);
    }

    public double getAverageRatingForCourse(Course course) {

        List<Rating> ratings = getAllByCourseId(course.getId());
        double numberOfRatings = ratings.size();

        if (ratings.isEmpty()) {
            return 0;
        }

        double allRatingsSum = ratings
                .stream().mapToInt(Rating::getRating)
                .sum();

        return  allRatingsSum / numberOfRatings;
    }

    public void create(Course course, User loggedUser, int rating) {

        if (getAllByUserId(loggedUser.getId())
                .stream()
                .map(Rating::getCourse)
                .anyMatch(c -> c.getId() == course.getId())) {

            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already rated Course with id: {%d}", loggedUser.getId(), course.getId()));
        }

        Rating courseRating = new Rating();
        courseRating.setCourse(course);
        courseRating.setUser(loggedUser);
        courseRating.setRating(rating);

        if (!loggedUser.hasCompletedCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has not completed Course with id: {%d}", courseRating.getUser().getId(), courseRating.getCourse().getId()));
        }

        ratingRepository.save(courseRating);
    }

    public void update(Rating rating, int newRating, User loggedUser) {

        User ratingCreator = rating.getUser();

        if (ratingCreator.getId() != loggedUser.getId()){
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not the creator of the Rating with id: {%d}", loggedUser.getId(), rating.getId()));
        }

        rating.setRating(newRating);
        ratingRepository.save(rating);
    }

    public void delete(Rating rating, User loggedUser) {

        if (rating.getUser().getId() != loggedUser.getId()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not the creator of the rating with id: {%d}",loggedUser.getId(), rating.getId()));
        }
        ratingRepository.delete(rating);
    }

}
