package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.Status;
import com.henrique.virtualteacher.repositories.AssignmentRepository;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.AssignmentServiceImpl;
import com.henrique.virtualteacher.services.implementation.CourseServiceImpl;
import com.henrique.virtualteacher.services.interfaces.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceImplTests {

    @Mock
    AssignmentRepository assignmentRepository;
    @Mock
    CourseRepository courseRepository;
    @Mock
    UserService userService;
    @Mock
    UserRepository userRepository;
    @Mock
    CourseServiceImpl courseService;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    AssignmentServiceImpl assignmentService;

    @Test
    public void getById_shouldThrowException_whenEntityNotFound() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();

        Assertions.assertThrows(EntityNotFoundException.class, () -> assignmentService.getById(mockAssignment.getId(), mockAssignment.getUser()));
    }

    @Test
    public void getById_shouldThrowException_whenUserIsNotAuthorized() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();
        User initiator = Helpers.createMockUser();
        initiator.setId(21);

        Mockito.when(assignmentRepository.findById(mockAssignment.getId())).thenReturn(Optional.of(mockAssignment));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getById(mockAssignment.getId(), initiator));
    }

    @Test
    public void getById_shouldReturnCorrectEntity() {
        Assignment assignment = Helpers.createMockGradedAssignment();
        User initiator = Helpers.createMockTeacher();

        Mockito.when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        Assignment result = assignmentService.getById(assignment.getId(), initiator);

        Assertions.assertAll(
                () -> Assertions.assertEquals(assignment.getId(), result.getId()),
                () -> Assertions.assertEquals(assignment.getLecture().getTitle(), result.getLecture().getTitle())
        );
    }

    @Test
    public void getByUserIdAndLectureId_shouldThrowException_whenEntityNotFound() {
        User initiator = Helpers.createMockTeacher();
        Lecture lecture = Helpers.createMockLecture();

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> assignmentService.getByUserIdAndLectureId(initiator.getId(), lecture.getId(), initiator));
    }

    @Test
    public void getByUserIdAndLectureId_shouldThrowException_when_InitiatorIsNotAuthorized() {
        User initiator = Helpers.createMockUser(21);

        User assignmentCreator = Helpers.createMockUser();
        Assignment assignment = Helpers.createMockGradedAssignment(assignmentCreator);
        Lecture assignmentLecture = assignment.getLecture();

        Mockito.when(assignmentRepository.getByUserIdAndLectureId(assignmentCreator.getId(), assignmentLecture.getId())).thenReturn(Optional.of(assignment));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getByUserIdAndLectureId(assignmentCreator.getId(), assignmentLecture.getId(), initiator));
    }

    @Test
    public void getByUserIdAndLectureId_shouldReturnCorrectEntity_whenInitiatorIsAuthorized() {
        User initiator = Helpers.createMockTeacher();

        User assignee = Helpers.createMockUser(21);
        Assignment assignment = Helpers.createMockPendingAssignment(assignee);
        Lecture lecture = assignment.getLecture();

        Mockito.when(assignmentRepository.getByUserIdAndLectureId(assignee.getId(), lecture.getId())).thenReturn(Optional.of(assignment));

        Assignment result = assignmentService.getByUserIdAndLectureId(assignee.getId(), lecture.getId(), initiator);

        Assertions.assertAll(
                () -> Assertions.assertEquals(assignment.getId(), result.getId()) ,
                () ->  Assertions.assertEquals(assignment.getLecture().getTitle(), result.getLecture().getTitle())
        );
    }

    @Test
    public void getAllUserAssignmentsForCourse_shouldThrowException_whenEntityNotFound() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();

        Assertions.assertThrows(EntityNotFoundException.class, () -> assignmentService.getAllUserAssignmentsForCourse(mockAssignment.getUser().getId(), mockAssignment.getLecture().getCourse().getId(),mockAssignment.getUser()));
    }

    @Test
    public void getAllUserAssignmentsForCourse_shouldThrowException_whenUserIsNotAuthorized(){
        Course course = Helpers.createMockCourse();
        User mockUser = Helpers.createMockUser();
        User otherMockUser  = Helpers.createMockUser();
        otherMockUser.setId(212);
        List<Assignment> assignments = Helpers.createMockAssignmentList();

        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseId(mockUser.getId(), course.getId())).thenReturn(assignments);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getAllUserAssignmentsForCourse(mockUser.getId(), course.getId(), otherMockUser));
    }

    @Test
    public void getAllUserAssignmentsForCourse_shouldReturnCorrectList_whenInitiatorIsAuthorized() {
        User initiator = Helpers.createMockTeacher();
        Course course = Helpers.createMockCourse();
        List<Assignment> assignments = Helpers.createMockAssignmentList(initiator);

        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseId(initiator.getId(), course.getId())).thenReturn(assignments);

        List<Assignment> resultList = assignmentService.getAllUserAssignmentsForCourse(initiator.getId(), course.getId(), initiator);

        Assertions.assertAll(
                () -> Assertions.assertEquals(assignments.get(0).getId(), resultList.get(0).getId()) ,
                () ->  Assertions.assertEquals(assignments.get(0).getLecture().getTitle(), resultList.get(0).getLecture().getTitle())
        );
    }

    @Test
    public void getAllUserGradedAssignmentsForCourse_shouldThrowException_whenUserIsNotAuthorized() {
        User mockUSer = Helpers.createMockTeacher();
        Course course = Helpers.createMockCourse();

        Assertions.assertThrows(EntityNotFoundException.class, () -> assignmentService.getAllUserGradedAssignmentsForCourse(21, course.getId(), mockUSer));
    }

    @Test
    public void getAllUserGradedAssignmentsForCourse_ShouldReturnCorrectEntityList() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        List<Assignment> assignmentList = Helpers.createGradedAssignmentList(mockUser);

        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(mockUser.getId(), mockCourse.getId(), Status.GRADED)).thenReturn(assignmentList);

        List<Assignment> resultList = assignmentService.getAllUserGradedAssignmentsForCourse(mockCourse.getId(), mockCourse.getId(), mockUser);

        Assertions.assertAll(
                () -> Assertions.assertEquals(assignmentList.get(0).getId(), resultList.get(0).getId()) ,
                () ->  Assertions.assertEquals(assignmentList.get(0).getLecture().getTitle(), resultList.get(0).getLecture().getTitle()),
                () -> Assertions.assertEquals(assignmentList.size(), resultList.size())
        );
    }


    @Test
    public void getAllGradedForCourse_shouldThrowException_when_userIsNotAuthorized() {
        User mockUser = Helpers.createMockUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getAllGradedForCourse(21, mockUser));
    }

    @Test
    public void getAllGradedForCourse_shouldReturnEntityList() {
        User initiator = Helpers.createMockTeacher();

        User student = Helpers.createMockUser(99);
        Course course = Helpers.createMockCourse();
        List<Assignment> assignmentList = Helpers.createGradedAssignmentList(student);

        Mockito.when(assignmentRepository.getAllByLectureCourseIdAndStatus(course.getId(), Status.GRADED)).thenReturn(assignmentList);

        List<Assignment> resultList = assignmentService.getAllGradedForCourse(course.getId(), initiator);

        Assertions.assertAll(
                () -> Assertions.assertEquals(assignmentList.get(0).getId(), resultList.get(0).getId()) ,
                () ->  Assertions.assertEquals(assignmentList.get(0).getLecture().getTitle(), resultList.get(0).getLecture().getTitle()),
                () -> Assertions.assertEquals(assignmentList.size(), resultList.size())
        );
    }

    @Test
    public void getAllPending_shouldReturn_EntityList() {
        User initiator = Helpers.createMockTeacher();
        List<Assignment> assignments = Helpers.createPendingAssignmentList(initiator);

        Mockito.when(assignmentRepository.getAllByStatus(Status.PENDING)).thenReturn(assignments);

        List<Assignment> result = assignmentService.getAllPending(initiator);

        Assertions.assertAll(
                () -> Assertions.assertEquals(assignments.get(0).getId(), result.get(0).getId()),
                () -> Assertions.assertEquals(assignments.size(), result.size())
        );
    }

    @Test
    public void getAllPending_shouldThrowException_whenUserIsNotTeacherOrAdmin() {
        User mockUser = Helpers.createMockUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getAllPending(mockUser));
    }

    @Test
    public void getAverageGradeForCourse_shouldThrowException_whenUserIsNotAuthorized() {
        User mockUser = Helpers.createMockUser();
        Course course = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getUserAverageGradeForCourse(21,course, mockUser));
    }

    @Test
    public void getAverageGradeForCourse_shouldReturn_exactPercentage() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        List<Assignment> assignments = Helpers.createMockAssignmentList(mockUser);

        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(1,1, Status.GRADED)).thenReturn(assignments);

        double result = assignmentService.getUserAverageGradeForCourse(mockUser.getId(), mockCourse, mockUser);

        Assertions.assertEquals(70.0, result);
    }

    @Test
    public void grade_shouldThrowException_whenInitiatorIsNotTeacher() {
        User mockUser = Helpers.createMockUser();
        Assignment assignment = Helpers.createMockGradedAssignment();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.grade(assignment, mockUser, 100));
    }

    @Test
    public void create_shouldThrowException_whenUserAlreadySubmittedAssignment() {
        Assignment assignment = Helpers.createMockGradedAssignment();
        assignment.getUser().setAssignments(List.of(assignment));

        Assertions.assertThrows(ImpossibleOperationException.class, () -> assignmentService.create(assignment));
    }

    @Test
    public void create_shouldCallRepository_andSaveEntity() {
        Assignment assignment = Helpers.createMockPendingAssignment();

        assignmentService.create(assignment);

        Mockito.verify(assignmentRepository, Mockito.times(1)).save(assignment);
    }

    @Test
    public void update_shouldThrowException_whenUserAlreadySubmittedAssignment() {
        Assignment assignment = Helpers.createMockGradedAssignment();
        User mockInitiator = Helpers.createMockUser();
        mockInitiator.setId(21);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.update("ww3",assignment, mockInitiator));
    }

    @Test
    public void delete_shouldThrowException_whenUserIsNotCreatorOrTeacher() {
        Assignment assignment = Helpers.createMockGradedAssignment();
        User mockInitiator = Helpers.createMockUser();
        mockInitiator.setId(21);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.delete(assignment, mockInitiator));
    }

    @Test
    public void getStudentAverageGradeForAllCourses_shouldReturnCorrectGrade() {
        User initiator = Helpers.createMockUser(21);
        List<CourseEnrollment> enrollments = Helpers.createCourseEnrollmentList(initiator);

        List<Assignment> assignments = Helpers.createGradedAssignmentList(enrollments.get(0).getCourse(), initiator);
        List<Assignment> assignments2 = Helpers.createGradedAssignmentList(enrollments.get(1).getCourse(), initiator);
        List<Assignment> assignments3 = Helpers.createGradedAssignmentList(enrollments.get(2).getCourse(), initiator);

        initiator.setAssignments(assignments);
        initiator.setCourseEnrollments(enrollments);

        Mockito.when(userService.getById(initiator.getId(), initiator)).thenReturn(initiator);
        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(initiator.getId(), enrollments.get(0).getId(), Status.GRADED)).thenReturn(assignments);

        double averageGrade = assignmentService.getStudentAverageGradeForAllCourses(initiator.getId(), initiator);

        Assertions.assertEquals(70, averageGrade);
    }

    @Test
    public void grade_shouldChangeAssignmentStatus_toGraded() {
        User initiator = Helpers.createMockTeacher();
        Assignment assignmentToGrade = Helpers.createMockPendingAssignment(Helpers.createMockUser());
        int grade = 75;

        assignmentService.grade(assignmentToGrade, initiator, grade);

        Assertions.assertEquals(Status.GRADED, assignmentToGrade.getStatus());
    }

    @Test
    public void update_shouldResetStatus() {
        User initiator = Helpers.createMockTeacher();
        Assignment assignment = Helpers.createMockGradedAssignment(Helpers.createMockUser());

        String newContent = "test string for content";

        assignmentService.update(newContent, assignment, initiator);

        Assertions.assertAll(
        () -> Assertions.assertEquals(assignment.getStatus(), Status.PENDING),
        () -> Assertions.assertEquals(0, assignment.getGrade()));
    }

    @Test
    public void delete_shouldCallRepository_ifUserIsAuthorized() {
        User initiator = Helpers.createMockUser(21);
        Assignment assignment = Helpers.createMockGradedAssignment(initiator);

        assignmentService.delete(assignment, initiator);

        Mockito.verify(assignmentRepository, Mockito.times(1)).delete(assignment);
    }

}
