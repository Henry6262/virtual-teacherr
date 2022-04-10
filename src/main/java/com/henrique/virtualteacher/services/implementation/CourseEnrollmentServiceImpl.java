package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.CourseEnrollment;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.CourseEnrollmentRepository;
import com.henrique.virtualteacher.services.interfaces.CourseEnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    @Override
    public CourseEnrollment getById(int id) {
        return courseEnrollmentRepository.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course Enrollment", "ID", String.valueOf(id)));
    }

    @Override
    public List<CourseEnrollment> getAllForUser(User loggedUser, int userToGetId) {
        checkUserIsAllowed(loggedUser, userToGetId);
        return courseEnrollmentRepository.getAllByUserId(userToGetId);
    }

    private void checkUserIsAllowed(User loggedUser, int userToGetId) {
        if (loggedUser.getId() != userToGetId && loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, does nto have access to the courses of user with id: %d", loggedUser.getId(), userToGetId));
        }
    }

    @Override
    public List<CourseEnrollment> getAllForCourse(User loggedUser, int courseToGetId) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id %d, does not have access to the users enrolled to course with id: %d", loggedUser.getId(), courseToGetId));
        }
        return courseEnrollmentRepository.getAllByCourseId(courseToGetId);
    }

    @Override
    public List<CourseEnrollment> getAllForUser(User loggedUser, int userToGetId, boolean completed) {
        checkUserIsAllowed(loggedUser, userToGetId);
        return courseEnrollmentRepository.getAllByUserIdAndCompleted(userToGetId, completed);
    }

    @Override
    public List<CourseEnrollment> getAllForCourse(User loggedUser, int courseToGetId, boolean completed) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id %d, does not have access to the users enrolled to course with id: %d", loggedUser.getId(), courseToGetId));
        }
        return courseEnrollmentRepository.getAllByCourseIdAndCompleted(courseToGetId, completed);
    }

    @Override
    public CourseEnrollment getUserCourseEnrollment(User loggedUser, Course enrolledCourse) {
        return courseEnrollmentRepository.getByUserIdAndCourseId(loggedUser.getId(), enrolledCourse.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id: %d, is not enrolled to course with id: %d", loggedUser.getId(), enrolledCourse.getId())));
    }

    @Override
    public CourseEnrollment enroll(User userToEnroll, Course courseToEnrollTo) {
        if (userToEnroll.isEnrolledInCourse(courseToEnrollTo)){
            throw new ImpossibleOperationException(String.format("User with id: {%d}, Is already enrolled in course with id: {%d}", userToEnroll.getId(), courseToEnrollTo.getId()));
        }

        CourseEnrollment newCourseEnrollment = new CourseEnrollment(userToEnroll, courseToEnrollTo);
        return courseEnrollmentRepository.save(newCourseEnrollment);
    }

    @Override
    public void leave(User leavingUser, Course courseToLeave) {
        if (!leavingUser.isEnrolledInCourse(courseToLeave)){
            throw new ImpossibleOperationException(String.format("User with id: %d cannot leave course with id: %d, as the user is not enrolled",leavingUser.getId(), courseToLeave.getId()));
        }
        CourseEnrollment toDelete = getUserCourseEnrollment(leavingUser, courseToLeave);
        courseEnrollmentRepository.delete(toDelete);
    }

}
