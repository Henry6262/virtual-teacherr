package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.Status;
import com.henrique.virtualteacher.repositories.AssignmentRepository;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.AssignmentServiceImpl;
import com.henrique.virtualteacher.services.implementation.CourseServiceImpl;
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
    public void getAllByUserIdAndCourseId_shouldThrowException_whenEntityNotFound() {
        Assignment mockAssignment = Helpers.createMockGradedAssignment();

        Assertions.assertThrows(EntityNotFoundException.class, () -> assignmentService.getAllByUserIdAndCourseId(mockAssignment.getUser().getId(), mockAssignment.getLecture().getCourse().getId(),mockAssignment.getUser()));
    }

    @Test
    public void getAllByUserIdAndCourseId_shouldThrowException_whenUserIsNotAuthorized(){
        Course course = Helpers.createMockCourse();
        User mockUser = Helpers.createMockUser();
        User otherMockUser  = Helpers.createMockUser();
        otherMockUser.setId(212);
        List<Assignment> assignments = Helpers.createMockAssignmentList();

        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseId(mockUser.getId(), course.getId())).thenReturn(assignments);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getAllByUserIdAndCourseId(mockUser.getId(), course.getId(), otherMockUser));
    }

    @Test
    public void getAllUserGradedAssignmentsForCourse_shouldThrowException_whenUserIsNotAuthorized() {
        User mockUSer = Helpers.createMockUser();
        Course course = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getAllUserGradedAssignmentsForCourse(21, course.getId(), mockUSer));
    }

    @Test
    public void getAllGradedForCourse_shouldThrowException_when_userIsNotAuthorized() {
        User mockUser = Helpers.createMockUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getAllGradedForCourse(21, mockUser));
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

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> assignmentService.getUserAverageGradeForCourse(21,1, mockUser));
    }

    @Test
    public void getAverageGradeForCourse_shouldReturn_exactPercentage() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        List<Assignment> assignments = Helpers.createMockAssignmentList();
        assignments.get(0).setGrade(50);
        assignments.get(1).setGrade(50);

        Mockito.when(assignmentRepository.getAllByUserIdAndLectureCourseIdAndStatus(1,1, Status.GRADED)).thenReturn(assignments);
        Mockito.when(courseService.getById(1)).thenReturn(mockCourse);

        double result = assignmentService.getUserAverageGradeForCourse(1,1, mockUser);

        Assertions.assertEquals(result, 80.0 );
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

}
