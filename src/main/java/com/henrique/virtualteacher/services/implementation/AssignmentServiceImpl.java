package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.Status;
import com.henrique.virtualteacher.models.UserModel;
import com.henrique.virtualteacher.repositories.AssignmentRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.AssignmentService;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.RatingService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

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
    public List<Assignment> getAllUserAssignmentsForCourse(int userId, int courseId, User loggedUser) {

        List<Assignment> userAssignmentsForCourse = assignmentRepository.getAllByUserIdAndLectureCourseId(userId, courseId);

        if (userAssignmentsForCourse.isEmpty()) {
            throw new EntityNotFoundException(String.format("Assignments with User with id: {%d}, does not have any submitted assignments to course with id: %d", userId, courseId));
        }
        checkUserIsAuthorized(loggedUser, userAssignmentsForCourse.get(0));

        return userAssignmentsForCourse;
    }

    @Override
    public List<Assignment> getAllUserGradedAssignmentsForCourse(int userId, int courseId, User loggedUser) {

        checkUserIsAuthorized(loggedUser, userId);

        List<Assignment> gradedAssignments = assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(userId, courseId, Status.GRADED);
        if (gradedAssignments.isEmpty()) {
            throw new EntityNotFoundException(String.format("User with id: %d, does not have any GRADED lectures for course with id: %d", userId, courseId));
        }
        return gradedAssignments;
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
    public double getUserAverageGradeForCourse(int userId, Course course, User loggedUser) {

        checkUserIsAuthorized(loggedUser, userId);

        List<Assignment> userGradedAssignmentsForCourse = assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(userId, course.getId(), Status.GRADED);

        double sum =  userGradedAssignmentsForCourse.stream()
                .mapToDouble(Assignment::getGrade)
                .sum();

        if (sum != 0) {
            return sum / userGradedAssignmentsForCourse.size();
        } else {
            return -1;
        }
    }

    private void checkUserIsAuthorized(User loggedUser, int userToGetId) {
        if (loggedUser.isNotTeacherOrAdmin() && loggedUser.getId() != userToGetId) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to get course information of User with id: %d", loggedUser.getId(), userToGetId));
        }
    }

    @Override
    public double getStudentAverageGradeForAllCourses(int userId, User loggedUser) {

        User userToGet = userService.getById(userId, loggedUser);

        double sum = 0;
        for (Course current : userToGet.getCompletedCourses()) {
            double averageForCurrent  = getUserAverageGradeForCourse(loggedUser.getId(), current, loggedUser);

            if (averageForCurrent != -1) {
                sum += averageForCurrent;
            }
        }

        return sum == 0 ? 0 : sum /userToGet.getCompletedCourses().size();

    }

    @Override
    public double getStudentAverageGradeForAllCourses(int userId, UserModel initiator) {
        User initiatorEntity = userService.getByEmail(initiator.getEmail());
        return getStudentAverageGradeForAllCourses(userId, initiatorEntity);
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
            resetAssignmentStatus(assignmentToUpdate);
        }
        assignmentToUpdate.setContent(newContent);
        assignmentRepository.save(assignmentToUpdate);
    }

    private void resetAssignmentStatus(Assignment assignmentToUpdate) {
        assignmentToUpdate.setStatus(Status.PENDING);
        assignmentToUpdate.setGrade(0);
    }

    @Override
    public void delete(Assignment assignment, User loggedUser) {

        checkUserIsAuthorized(loggedUser, assignment);
        assignmentRepository.delete(assignment);
    }
}
