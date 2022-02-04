package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopics;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.text.ParseException;
import java.util.List;

public interface CourseService {

    Course getById(int id);

    Course getByTitle(String title);

    List<Course> getAll ();

    List<Course> getAllByTopic(EnumTopics topic);

    List<Course> getAllByEnabled(boolean isEnabled);

    List<Course> getAllByDifficulty(int difficultyLevel);

    int getPercentageOfCompletedCourseLectures(User loggedUser, Course course);

    void create(CourseModel course, User loggedUser);

    void update(CourseModel courseModel ,Course course , User loggedUser) throws ParseException;

    void delete(Course course, User loggedUser);

    void enableCourse(Course course, User loggedUser);

    void disableCourse(Course course, User loggedUser);

    void enroll(Course course, User loggedUser);

    void complete(Course course, User loggedUser);

    void addLectureToCourse(Lecture lecture, int courseId);

    void verifyUserHasCompletedAllCourseLectures(User user, Course course);

    void verifyUserIsEnrolledToCourse(User loggedUser, Course course);

}
