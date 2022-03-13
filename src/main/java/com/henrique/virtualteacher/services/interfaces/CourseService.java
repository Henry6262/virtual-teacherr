package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumDifficulty;
import com.henrique.virtualteacher.models.EnumTopic;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface CourseService {

    List<CourseModel> mapAllToModel(List<Course> courses, User loggedUser, boolean includeCompletionAmount);

    List<CourseModel> getTopTheeCoursesByRating();

    Course getById(int id);

    Course getByTitle(String title);

    List<Course> getAll ();

    List<CourseModel> getAllByTopic(EnumTopic topic);

    List<CourseModel> getAllByEnabled(boolean isEnabled, Optional<User> loggedUser);

    List<Course> getAllByDifficulty(EnumDifficulty difficultyLevel);

    int getPercentageOfCompletedCourseLectures(User loggedUser, Course course);

    void create(CourseModel course, User loggedUser);

    void update(CourseModel courseModel ,Course course , User loggedUser) throws ParseException;

    void delete(Course course, User loggedUser);

    void enableCourse(Course course, User loggedUser);

    void disableCourse(Course course, User loggedUser);

    void enroll(Course course, User loggedUser);

    void complete(Course course, User loggedUser);

    void upload (MultipartFile file, int courseId, User loggedUser) throws IOException;

    void addLectureToCourse(Lecture lecture, int courseId);

    void verifyUserHasCompletedAllCourseLectures(User user, Course course);

    void verifyUserIsEnrolledToCourse(User loggedUser, Course course);

}
