package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopics;

import java.security.Principal;
import java.util.List;

public interface CourseService {

    Course getById(int id, User loggedUser);

    Course getByTitle(String title);

    List<Course> getAll ();

    List<Course> getByTopic(EnumTopics topic);

    List<Course> getByEnabled(boolean isEnabled);

    List<Course> getByDifficulty(int difficultyLevel);

    boolean create(CourseModel course);

    void update(Course course);

    void delete(Course course);

    void enroll(Principal principal, int courseId);

}
