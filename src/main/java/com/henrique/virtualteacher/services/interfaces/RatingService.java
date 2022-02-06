package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Rating;
import com.henrique.virtualteacher.entities.User;

import java.util.List;

public interface RatingService {

    Rating getById(int id);

    List<Rating> getAllByCourseId(int courseId);

    List<Rating> getAllByUserId(int userId);

    void create(Course course, User user, int rating);

    void update(Rating rating, int newRating, User loggedUser);

    void delete(Rating rating, User loggedUser);

    double getAverageRatingForCourse(Course course);

}
