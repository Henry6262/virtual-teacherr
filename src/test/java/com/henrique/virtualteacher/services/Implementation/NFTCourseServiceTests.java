package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.NFTCourse;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.NFTCourseRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.NFTCourseServiceImpl;
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
public class NFTCourseServiceTests {

    @Mock
    NFTCourseRepository NFTCourseRepository;

    @InjectMocks
    NFTCourseServiceImpl courseEnrollmentService;


    @Test
    public void getById_shouldCallRepository() {
        NFTCourse NFTCourse = Helpers.createMockCourseEnrollment();
        Mockito.when(NFTCourseRepository.getById(1)).thenReturn(Optional.of(NFTCourse));

        NFTCourse result = courseEnrollmentService.getById(NFTCourse.getId());
        Assertions.assertAll(
                () -> Assertions.assertEquals(result.getId(), NFTCourse.getId()),
                () -> Assertions.assertEquals(result.getCourse().getId(), NFTCourse.getCourse().getId()),
                () -> Assertions.assertEquals(result.getOwner().getId(), NFTCourse.getOwner().getId())
        );
    }

    @Test
    public void getAllForUser_should_throwExceptionWhen_UserIsNotAuthorized() {
        User initiator = Helpers.createMockUser(21);
        User userBeingAccessed = Helpers.createMockUser(1);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseEnrollmentService.getAllForUser(initiator, userBeingAccessed.getId()));
    }

    @Test
    public void getAllForUser_ShouldThrowException_when_initiatorIsNotAuthorized() {
        User initiator = Helpers.createMockUser(21);
        User userBeingAccessed = Helpers.createMockUser(99);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseEnrollmentService.getAllForUser(initiator, userBeingAccessed.getId(),false));
    }

    @Test
    public void getAllForUser_shouldReturnEntityList() {
        User initiator = Helpers.createMockTeacher();
        User beingAccessed = Helpers.createMockUser(21);
        List<NFTCourse> enrollments = Helpers.createCourseEnrollmentList(beingAccessed);

        Mockito.when(NFTCourseRepository.getAllByOwnerIdAndCompleted(beingAccessed.getId(), false)).thenReturn(enrollments);

        List<NFTCourse> result = courseEnrollmentService.getAllForUser(initiator, beingAccessed.getId(), false);

        Assertions.assertAll(
                () -> Assertions.assertEquals(enrollments.get(0).getId(), result.get(0).getId()),
                () -> Assertions.assertEquals(enrollments.size(), result.size()));
    }

    @Test
    public void getAllForUser_shouldReturnCorrectList_whenInitiatorIsOwner() {
        User initiator = Helpers.createMockUser();
        List<NFTCourse> mockNFTCourses = Helpers.createCourseEnrollmentList(initiator);

        Mockito.when(NFTCourseRepository.getAllByOwnerId(initiator.getId())).thenReturn(mockNFTCourses);

        List<NFTCourse> resultList = courseEnrollmentService.getAllForUser(initiator, initiator.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(resultList.get(1).getId(), mockNFTCourses.get(1).getId()),
                () -> Assertions.assertEquals(resultList.get(2).getId(), mockNFTCourses.get(2).getId())
        );
    }

    @Test
    public void getAllForUser_shouldReturnCorrectList_whenInitiatorIsTeacher() {
        User initiator = Helpers.createMockTeacher();
        User enrolledUser = Helpers.createMockUser(21);
        List<NFTCourse> mockNFTCourses = Helpers.createCourseEnrollmentList(enrolledUser);

        Mockito.when(NFTCourseRepository.getAllByOwnerId(enrolledUser.getId())).thenReturn(mockNFTCourses);

        List<NFTCourse> resultList = courseEnrollmentService.getAllForUser(initiator, enrolledUser.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(resultList.get(1).getId(), mockNFTCourses.get(1).getId()),
                () -> Assertions.assertEquals(resultList.get(2).getId(), mockNFTCourses.get(2).getId())
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
        List<NFTCourse> NFTCourses = Helpers.createCourseEnrollmentList(courseCreator);

        Mockito.when(courseEnrollmentService.getAllForCourse(mockTeacher, courseToGet.getId())).thenReturn(NFTCourses);

        List<NFTCourse> resultList = courseEnrollmentService.getAllForCourse(mockTeacher, courseToGet.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(resultList.get(1).getId(), NFTCourses.get(1).getId()),
                () -> Assertions.assertEquals(resultList.get(2).getCourse().getTitle(), NFTCourses.get(2).getCourse().getTitle()),
                () -> Assertions.assertEquals(resultList.get(3).getOwner().getId(), NFTCourses.get(3).getOwner().getId())
        );
    }

    @Test
    public void getAllForCourse_shouldThrowException_when_initiatorIsNotAuthorized() {
        User initiator = Helpers.createMockUser(21);
        Course course = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseEnrollmentService.getAllForCourse(initiator, course.getId(), false));
    }

    @Test
    public void getAllForCourse_shouldReturnEntityList() {
        User initiator = Helpers.createMockTeacher();
        Course beingAccessed = Helpers.createMockCourse();
        List<NFTCourse> enrollments = Helpers.createCourseEnrollmentList(Helpers.createMockUser());

        Mockito.when(NFTCourseRepository.getAllByCourseIdAndCompleted(beingAccessed.getId(), false)).thenReturn(enrollments);

        List<NFTCourse> result = courseEnrollmentService.getAllForCourse(initiator, beingAccessed.getId(), false);
    }

    @Test
    public void getUserCourseEnrollment_shouldThrowException_when_EntityNotFound() {
        User mockUser = Helpers.createMockUser();
        Course courseToGet = Helpers.createMockCourse();

        Assertions.assertThrows(EntityNotFoundException.class, () -> courseEnrollmentService.getUserOwnedNFTCourse(mockUser, courseToGet));
    }

    @Test
    public void getUserCourseEnrollment_shouldReturnEntity_whenExisting() {
        User enrolledUser = Helpers.createMockUser(21);
        Course mockCourse = Helpers.createMockCourse();
        NFTCourse NFTCourse =  Helpers.createMockCourseEnrollment(enrolledUser, mockCourse);

        Mockito.when(NFTCourseRepository.getByOwnerIdAndCourseId(enrolledUser.getId(), mockCourse.getId())).thenReturn(Optional.of(NFTCourse));

        NFTCourse result = courseEnrollmentService.getUserOwnedNFTCourse(enrolledUser, mockCourse);

        Assertions.assertAll(
                () -> Assertions.assertEquals(result.getId(), NFTCourse.getId()),
                () -> Assertions.assertEquals(result.getOwner().getId(), NFTCourse.getOwner().getId()),
                () -> Assertions.assertEquals(result.getCourse().getTitle(), NFTCourse.getCourse().getTitle())
        );
    }

    @Test
    public void enroll_shouldThrowException_when_UserIsAlreadyEnrolledToCourse() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        mockUser.purchaseCourse(mockCourse);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseEnrollmentService.purchase(mockUser, mockCourse));
    }

    @Test
    public void enroll_shouldSaveEntity_whenUserIsNotEnrolled() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        NFTCourse NFTCourse = Helpers.createMockCourseEnrollment(mockUser, mockCourse);

        Mockito.when(NFTCourseRepository.save(Mockito.any(NFTCourse.class))).thenReturn(NFTCourse);

        courseEnrollmentService.purchase(mockUser, mockCourse);

        Mockito.verify(NFTCourseRepository, Mockito.times(1)).save(Mockito.any());
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
        NFTCourse NFTCourse = Helpers.createMockCourseEnrollment(mockUser, mockCourse);
        mockUser.purchaseCourse(mockCourse);

        Mockito.when(NFTCourseRepository.getByOwnerIdAndCourseId(mockUser.getId(), mockCourse.getId())).thenReturn(Optional.of(NFTCourse));

        courseEnrollmentService.leave(mockUser, mockCourse);

        Mockito.verify(NFTCourseRepository, Mockito.times(1)).delete(NFTCourse);
    }



}
