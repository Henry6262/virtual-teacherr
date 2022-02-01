package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CourseModel;

import java.util.List;

public interface AssignmentService {

    Assignment getById(int id, User loggedUser);

    Assignment getByUserIdAndLectureId(int userId, int lectureId, User loggedUser);

    List<Assignment> getAllByUserIdAndCourseId(int userId, int courseId, User loggedUser);

    List<Assignment> getAllUserGradedAssignmentsForCourse(int userId, int courseId, User loggedUser);

    List<Assignment> getAllGradedForCourse(int courseId, User loggedUser);

    List<Assignment> getAllPending(User loggedUser);



    double getUserAverageGradeForCourse(int userId, int courseId, User LoggedUser);

    void grade(Assignment assignment, User loggedUser, int grade);

    void create(Assignment assignment, User loggedUser); //fixme: needs to have teacher role to approve

    void update(String newContent, Assignment gradeToUpdate, User loggedUser);

    void delete(Assignment assignment, User loggedUser);

}
