package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.CourseRating;
import com.henrique.virtualteacher.entities.User;

import java.util.List;

public interface RatingService {

    CourseRating getById(int id);

    List<CourseRating> getAllByCourseId(int courseId);

    List<CourseRating> getAllByUserId(int userId);

    void create(Course course, User user, int rating);

    double getAverageRatingForCourse(Course course);
}
