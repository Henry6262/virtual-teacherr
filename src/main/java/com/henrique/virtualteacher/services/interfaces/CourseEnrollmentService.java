package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.CourseEnrollment;
import com.henrique.virtualteacher.entities.User;

import java.util.List;

public interface CourseEnrollmentService {

    CourseEnrollment getById(int id);

    List<CourseEnrollment> getAllForUser(User loggedUser, int userToGetId);

    List<CourseEnrollment> getAllForCourse(User loggedUser, int courseToGetId);

    List<CourseEnrollment> getAllForUser(User loggedUser, int userToGetId, boolean completed);

    List<CourseEnrollment> getAllForCourse(User loggedUser, int courseToGet, boolean completed);

    CourseEnrollment getUserCourseEnrollment(User loggedUser, Course enrolledCourse);

    void enroll(User userToEnroll, Course courseToEnrollTo);

    void leave(User leavingUser, Course courseToLeave);

}
