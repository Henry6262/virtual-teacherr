package com.henrique.virtualteacher.services;

import com.henrique.virtualteacher.Helpers;
import com.henrique.virtualteacher.VirtualTeacherApplication;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.EnumTopics;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.LectureRepository;
import com.henrique.virtualteacher.services.implementation.CourseServiceImpl;
import com.henrique.virtualteacher.services.implementation.LectureServiceImpl;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import org.hibernate.id.uuid.Helper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VirtualTeacherApplication.class)

public class CourseServiceTests {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    LectureServiceImpl lectureService;

    @InjectMocks
    CourseServiceImpl courseService;

    @Test
    public void getById_should_throwException_when_course_doesNot_exist() {
        Course course = Helpers.createMockCourse();

        Assertions.assertThrows(EntityNotFoundException.class, () -> courseService.getById(course.getId()));
    }


    @Test
    public void getByTitle_should_throwExceptionWhen_courseWithTitle_doesNot_exist() {
        Course course = Helpers.createMockCourse();

        Assertions.assertThrows(EntityNotFoundException.class,() -> courseService.getByTitle(course.getTitle()));
    }

    @Test
    public void getAll_shouldReturn_emptyList_when_noCourses_exist() {

        Assertions.assertEquals(0, courseService.getAll().size());
    }

    @Test
    public void getAll_shouldReturn_list_when_courses_exist() {
        List<Course> mockCourseList = Helpers.createMockCourseList();
        Mockito.when(courseRepository.findAll()).thenReturn(mockCourseList);

        List<Course> result = courseService.getAll();
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void getAllByDifficulty_should_callRepository_andReturn_list() {
        List<Course> mockCourses = Helpers.createMockCourseList();

        Mockito.when(courseRepository.findByDifficulty(4)).thenReturn(mockCourses);
        courseService.getAllByDifficulty(4);
        Mockito.verify(courseRepository, Mockito.times(1))
                .findByDifficulty(4);
    }

    @Test
    public void getAllByTopic_should_callRepository_andReturn_list() {
        List<Course> mockCourses = Helpers.createMockCourseList();

        Mockito.when(courseRepository.findByTopic(EnumTopics.JAVA)).thenReturn(mockCourses);
        courseService.getAllByTopic(EnumTopics.JAVA);
        Mockito.verify(courseRepository, Mockito.times(1))
                .findByTopic(EnumTopics.JAVA);
    }

    @Test
    public void getAllByEnabled_should_callRepository_andReturn_list() {
        List<Course> mockCourses = Helpers.createMockCourseList();

        Mockito.when(courseRepository.findByEnabled(true)).thenReturn(mockCourses);
        courseService.getAllByEnabled(true);
        Mockito.verify(courseRepository, Mockito.times(1))
                .findByEnabled(true);
    }

    @Test
    public void verifyUserIsEnrolledToCourse_shouldThrowException_when_userIsNotEnrolledToCourse() {
        User mockUser = Helpers.createMockUser();
        Course course = Helpers.createMockCourse();
        mockUser.enrollToCourse(course);

        Course otherCourse = Helpers.createMockCourse();
        otherCourse.setId(2);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.verifyUserIsEnrolledToCourse(mockUser, otherCourse));
    }

    @Test
    public void verifyUserHasCompletedAllCourseLectures_shouldThrowException_when_userDidNotCompleteAllLectures() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        List<Lecture> mockCourseLectures  = Helpers.createMockLectureList(mockCourse);

        mockCourse.setCourseLectures(List.copyOf(mockCourseLectures));
        mockCourseLectures.remove(0);
        mockUser.enrollToCourse(mockCourse);
        mockUser.setCompletedLectures(Set.copyOf(mockCourseLectures));

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.verifyUserHasCompletedAllCourseLectures(mockUser, mockCourse));
    }

    @Test
    public void getPercentageOfCompletedLectures_shouldReturn_exactPercentage() {

        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        List<Lecture> mockLectures = Helpers.createMockLectureList(mockCourse);

        mockCourse.setCourseLectures(List.copyOf(mockLectures));
        mockLectures.remove(0);

        mockUser.setCompletedLectures(Set.copyOf(mockLectures));

        Assertions.assertEquals(75.00, courseService.getPercentageOfCompletedCourseLectures(mockUser, mockCourse));
    }

    @Test
    public void enroll_shouldThrowException_when_userIsAlreadyEnrolledToCourse() {

        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        mockUser.enrollToCourse(mockCourse);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.enroll(mockCourse, mockUser));
    }

    @Test
    public void complete_shouldThrowException_when_userIsNotEnrolled() {

        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.complete(mockCourse, mockUser));
    }

    @Test
    public void completeCourse_shouldThrowException_when_courseIsAlreadyCompleted() {

        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();
        mockUser.enrollToCourse(mockCourse);
        mockUser.completeCourse(mockCourse);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.complete(mockCourse, mockUser));
    }

    @Test
    public void disableCourse_shouldThrowException_when_initiatorIsNot_teacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseService.disableCourse(mockCourse, mockUser));
    }

    @Test
    public void disableCourse_shouldThrowException_when_courseIsAlreadyDisabled() {
        User mockUser = Helpers.createMockTeacher();
        Course mockCourse = Helpers.createMockCourse();
        mockCourse.setEnabled(false);

        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.disableCourse(mockCourse, mockUser));
    }

    @Test
    public void enableCourse_shouldThrowException_when_InitiatorIsNot_teacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class,() -> courseService.enableCourse(mockCourse, mockUser));
    }

    @Test
    public void enableCourse_shouldThrowException_when_courseIsAlreadyEnabled() {
        User mockUser = Helpers.createMockTeacher();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(ImpossibleOperationException.class,() -> courseService.enableCourse(mockCourse, mockUser));
    }

    @Test
    public void delete_shouldThrowException_when_initiatorIsNot_teacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseService.delete(mockCourse, mockUser));
    }


}
