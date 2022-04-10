package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.CourseEnrollment;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.CourseEnrollmentRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.CourseEnrollmentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CourseEnrollmentServiceTests {

    @Mock
    CourseEnrollmentRepository courseEnrollmentRepository;

    @InjectMocks
    CourseEnrollmentServiceImpl courseEnrollmentService;


    @Test
    public void getById_shouldCallRepository() {
        CourseEnrollment courseEnrollment = Helpers.createMockCourseEnrollment();
        Mockito.when(courseEnrollmentRepository.getById(1)).thenReturn(Optional.of(courseEnrollment));

        CourseEnrollment result = courseEnrollmentService.getById(courseEnrollment.getId());
        Assertions.assertAll(
                () -> Assertions.assertEquals(result.getId(), courseEnrollment.getId()),
                () -> Assertions.assertEquals(result.getCourse().getId(), courseEnrollment.getCourse().getId()),
                () -> Assertions.assertEquals(result.getUser().getId(), courseEnrollment.getUser().getId())
        );
    }

    @Test
    public void getAllForUser_should_throwExceptionWhen_UserIsNotAuthorized() {
        User initiator = Helpers.createMockUser(21);
        User userBeingAccessed = Helpers.createMockUser(1);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseEnrollmentService.getAllForUser(initiator, userBeingAccessed.getId()));
    }

    @Test
    public void getAllForUser_shouldReturnCorrectList_whenInitiatorIsOwner() {
        User initiator = Helpers.createMockUser();
        List<CourseEnrollment> mockCourseEnrollments = Helpers.createCourseEnrollmentList(initiator);

        Mockito.when(courseEnrollmentRepository.getAllByUserId(initiator.getId())).thenReturn(mockCourseEnrollments);

        List<CourseEnrollment> resultList = courseEnrollmentService.getAllForUser(initiator, initiator.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(resultList.get(1).getId(), mockCourseEnrollments.get(1).getId()),
                () -> Assertions.assertEquals(resultList.get(2).getId(), mockCourseEnrollments.get(2).getId())
        );
    }

    @Test
    public void getAllForUser_shouldReturnCorrectList_whenInitiatorIsTeacher() {
        User initiator = Helpers.createMockTeacher();
        User enrolledUser = Helpers.createMockUser(21);
        List<CourseEnrollment> mockCourseEnrollments = Helpers.createCourseEnrollmentList(enrolledUser);

        Mockito.when(courseEnrollmentRepository.getAllByUserId(enrolledUser.getId())).thenReturn(mockCourseEnrollments);

        List<CourseEnrollment> resultList = courseEnrollmentService.getAllForUser(initiator, enrolledUser.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(resultList.get(1).getId(), mockCourseEnrollments.get(1).getId()),
                () -> Assertions.assertEquals(resultList.get(2).getId(), mockCourseEnrollments.get(2).getId())
        );
    }

    @Test
    public void getAllByCourse_should_throwException_whenInitiator_isNotTeacherOrAdmin() {
        User initiator = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse(initiator);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseEnrollmentService.getAllForCourse(initiator, mockCourse.getId()));
    }

    @Test
    public void getAllByCourse_shouldReturnList_whenInitiatorIsTeacherOrAdmin() {
        User mockTeacher = Helpers.createMockTeacher();
        User courseCreator = Helpers.createMockUser(21);
        Course courseToGet = Helpers.createMockCourse(courseCreator);
        List<CourseEnrollment> courseEnrollments = Helpers.createCourseEnrollmentList(courseCreator);

        Mockito.when(courseEnrollmentService.getAllForCourse(mockTeacher, courseToGet.getId())).thenReturn(courseEnrollments);

        List<CourseEnrollment> resultList = courseEnrollmentService.getAllForCourse(mockTeacher, courseToGet.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(resultList.get(1).getId(), courseEnrollments.get(1).getId()),
                () -> Assertions.assertEquals(resultList.get(2).getCourse().getTitle(), courseEnrollments.get(2).getCourse().getTitle()),
                () -> Assertions.assertEquals(resultList.get(3).getUser().getId(), courseEnrollments.get(3).getUser().getId())
        );
    }

    @Test
    public void getUserCourseEnrollment_shouldThrowException_when_EntityNotFound() {
        User mockUser = Helpers.createMockUser();
        Course courseToGet = Helpers.createMockCourse();

        Assertions.assertThrows(EntityNotFoundException.class, () -> courseEnrollmentService.getUserCourseEnrollment(mockUser, courseToGet));
    }

    @Test
    public void getUserCourseEnrollment_shouldReturnEntity_whenExisting() {
        User enrolledUser = Helpers.createMockUser(21);
        Course mockCourse = Helpers.createMockCourse();
        CourseEnrollment courseEnrollment =  Helpers.createMockCourseEnrollment(enrolledUser, mockCourse);

        Mockito.when(courseEnrollmentRepository.getByUserIdAndCourseId(enrolledUser.getId(), mockCourse.getId())).thenReturn(Optional.of(courseEnrollment));

        CourseEnrollment result = courseEnrollmentService.getUserCourseEnrollment(enrolledUser, mockCourse);

        Assertions.assertAll(
                () -> Assertions.assertEquals(result.getId(), courseEnrollment.getId()),
                () -> Assertions.assertEquals(result.getUser().getId(), courseEnrollment.getUser().getId()),
                () -> Assertions.assertEquals(result.getCourse().getTitle(), courseEnrollment.getCourse().getTitle())
        );
    }

    @Test
    public void enroll_shouldThrowException_when_UserIsAlreadyEnrolledToCourse() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        mockUser.enrollToCourse(mockCourse);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseEnrollmentService.enroll(mockUser, mockCourse));
    }

    @Test
    public void enroll_shouldSaveEntity_whenUserIsNotEnrolled() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        CourseEnrollment courseEnrollment = Helpers.createMockCourseEnrollment(mockUser, mockCourse);

        Mockito.when(courseEnrollmentRepository.save(Mockito.any(CourseEnrollment.class))).thenReturn(courseEnrollment);

        CourseEnrollment result = courseEnrollmentService.enroll(mockUser, mockCourse);

        Assertions.assertAll(
                () -> Assertions.assertEquals(courseEnrollment.getId(), result.getId()),
                () -> Assertions.assertEquals(courseEnrollment.getCourse().getTitle(), result.getCourse().getTitle()),
                () -> Assertions.assertEquals(courseEnrollment.getUser().getId(), result.getUser().getId())
        );
    }

    @Test
    public void leave_shouldThrowException_whenUserIsNotEnrolledToCourse() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseEnrollmentService.leave(mockUser, mockCourse));
    }

    @Test
    public void leave_shouldCallRepository_when_userIsEnrolledToCourse() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        CourseEnrollment courseEnrollment = Helpers.createMockCourseEnrollment(mockUser, mockCourse);
        mockUser.enrollToCourse(mockCourse);

        Mockito.when(courseEnrollmentRepository.getByUserIdAndCourseId(mockUser.getId(), mockCourse.getId())).thenReturn(Optional.of(courseEnrollment));

        courseEnrollmentService.leave(mockUser, mockCourse);

        Mockito.verify(courseEnrollmentRepository, Mockito.times(1)).delete(courseEnrollment);
    }



}
