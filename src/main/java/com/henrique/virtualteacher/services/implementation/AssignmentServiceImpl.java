package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.Status;
import com.henrique.virtualteacher.repositories.AssignmentRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.AssignmentService;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.RatingService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final ModelMapper modelMapper;
    private final Logger logger;
    private final RatingService ratingService;

    @Override
    public Assignment getById(int id, User loggedUser) {

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment", "id", String.valueOf(id)));

        checkUserIsAuthorized(loggedUser, assignment);

        return assignment;
    }

    private void checkUserIsAuthorized(User loggedUser, Assignment assignment) {
        if (loggedUser.isNotTeacherOrAdmin() && assignment.getUser().getId() != loggedUser.getId()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not allowed to access assignment with id: {%d}", loggedUser.getId(), assignment.getId()));
        }
    }

    @Override
    public Assignment getByUserIdAndLectureId(int userId, int lectureId, User loggedUser) {

        Assignment assignment = assignmentRepository.getByUserIdAndLectureId(userId, lectureId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment", "id", String.valueOf(lectureId)));

        checkUserIsAuthorized(loggedUser, assignment);

        return assignment;

    }

    @Override
    public List<Assignment> getAllByUserIdAndCourseId(int userId, int courseId, User loggedUser) {

        List<Assignment> grades = assignmentRepository.getAllByUserIdAndLectureCourseId(userId, courseId);

        if (grades.isEmpty()) {
            throw new EntityNotFoundException(String.format("Assignment with User with id: {%d}, and Course with id: {%d}, has not been found", userId, courseId));
        }
        checkUserIsAuthorized(loggedUser, grades.get(0));

        return grades;
    }

    @Override
    public List<Assignment> getAllUserGradedAssignmentsForCourse(int userId, int courseId, User loggedUser) {

        if (loggedUser.getId() != userId && !loggedUser.isTeacher()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to access the Assignments of the User with id: {%d}", loggedUser.getId(), userId));
        }

        return assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(userId, courseId, Status.GRADED);
        //fixme: maybe need to check if list is empty, and throw exception
    }

    @Override
    public int getUserCompletedCourseLectures(int userId, int courseId, User loggedUser){

        if (loggedUser.getId() != userId && !loggedUser.isTeacher()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to access the Assignments of the User with id: {%d}", loggedUser.getId(), userId));
        }

        return assignmentRepository.getAllByUserIdAndLectureCourseId(userId, courseId).size();
    }

    @Override
    public List<Assignment> getAllGradedForCourse(int courseId, User loggedUser) {

        if (!loggedUser.isTeacher()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to get all graded Assignments for course with id: {%d}", loggedUser.getId(), courseId));
        }
        return assignmentRepository.getAllByLectureCourseIdAndStatus(courseId, Status.GRADED);
    }

    @Override
    public List<Assignment> getAllPending(User loggedUser) {
        if (!loggedUser.isTeacher()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to get all pending Assignments", loggedUser.getId()));
        }

        return assignmentRepository.getAllByStatus(Status.PENDING);
    }

    @Override
    public double getUserAverageGradeForCourse(int userId, int courseId, User loggedUser) {

        List<Assignment> userGradedAssignmentsForCourse = getAllUserGradedAssignmentsForCourse(userId, courseId, loggedUser);

        Course course = courseService.getById(courseId);

        double sum =  userGradedAssignmentsForCourse.stream()
                .mapToDouble(Assignment::getGrade)
                .sum();

        return sum / userGradedAssignmentsForCourse.size();
    }

    @Override
    public void grade(Assignment assignment, User loggedUser, int grade) {

        if (!loggedUser.isTeacher()){
            throw new UnauthorizedOperationException("User with id: {%d}, is not authorized to grade Assignments");
        }

        assignment.grade(grade);
        assignmentRepository.save(assignment);
    }


    @Override
    public void create(Assignment assignment) {

        if (assignment.getUser().hasAssignment(assignment)){
            throw new ImpossibleOperationException(String.format("User with id: {%d}, already has already Submitted an Assignment for the lecture with id: {%d}", assignment.getUser().getId(), assignment.getLecture().getId()));
        }

        assignmentRepository.save(assignment);
    }

    @Override
    public void update(String newContent, Assignment assignmentToUpdate, User loggedUser) {

        checkUserIsAuthorized(loggedUser, assignmentToUpdate);

        if (assignmentToUpdate.getStatus().equals(Status.GRADED)) {
            assignmentToUpdate.setStatus(Status.PENDING);
            assignmentToUpdate.setGrade(0);
        }
        assignmentToUpdate.setContent(newContent);
        assignmentRepository.save(assignmentToUpdate);
    }

    @Override
    public void delete(Assignment assignment, User loggedUser) {

        checkUserIsAuthorized(loggedUser, assignment);
        assignmentRepository.delete(assignment);
    }
}
