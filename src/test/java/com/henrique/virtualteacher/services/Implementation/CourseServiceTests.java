package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.configurations.CloudinaryConfig;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.Transaction;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumDifficulty;
import com.henrique.virtualteacher.models.EnumTopic;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.LectureRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.CourseServiceImpl;
import com.henrique.virtualteacher.services.implementation.LectureServiceImpl;
import com.henrique.virtualteacher.services.interfaces.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTests {

    @Mock
    UserRepository userRepository;
    @Mock
    CourseRepository courseRepository;
    @Mock
    LectureRepository lectureRepository;
    @Mock
    LectureServiceImpl lectureService;
    @Mock
    ModelMapper modelMapper;
    @Mock
    RatingService ratingService;
    @Mock
    UserService userService;
    @Mock
    WalletService walletService;
    @Mock
    TransactionService transactionService;
    @Mock
    NFTCourseService enrollmentService;
    @Mock
    Logger logger;
    @Mock
    CloudinaryConfig cloudinaryConfig;

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

        Mockito.when(courseRepository.findByDifficulty(EnumDifficulty.ADVANCED)).thenReturn(mockCourses);
        courseService.getAllByDifficulty(EnumDifficulty.ADVANCED);
        Mockito.verify(courseRepository, Mockito.times(1))
                .findByDifficulty(EnumDifficulty.ADVANCED);
    }

    @Test
    public void getAllByTopic_should_callRepository_andReturn_list() {
        List<Course> mockCourses = Helpers.createMockCourseList();

        Mockito.when(courseRepository.findByTopic(EnumTopic.JAVA)).thenReturn(mockCourses);
        List<CourseModel> result = courseService.getAllByTopic(EnumTopic.JAVA);

        Assertions.assertEquals(mockCourses.size(), result.size());
    }

    @Test
    public void getAllByEnabled_should_callRepository_andReturn_list() {
        List<Course> mockCourses = Helpers.createMockCourseList();

        Mockito.when(courseRepository.findByEnabled(true)).thenReturn(mockCourses);
        Mockito.when(ratingService.getAverageRatingForCourse(Mockito.any())).thenReturn(Mockito.any(Double.class));

        courseService.getAllByEnabled(true, Optional.empty());
        Mockito.verify(courseRepository, Mockito.times(1))
                .findByEnabled(true);
    }

    @Test
    public void verifyUserIsEnrolledToCourse_shouldThrowException_when_userIsNotEnrolledToCourse() {
        User mockUser = Helpers.createMockUser();
        Course course = Helpers.createMockCourse();
        mockUser.purchaseCourse(course);

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
        mockUser.purchaseCourse(mockCourse);
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

//    @Test
//    public void enroll_shouldThrowException_when_userIsAlreadyEnrolledToCourse() {
//
//        User mockUser = Helpers.createMockUser();
//        Course mockCourse = Helpers.createMockCourse();
//        mockUser.enrollToCourse(mockCourse);
//
//        Assertions.assertThrows(ImpossibleOperationException.class, () -> courseService.enroll(mockCourse, mockUser));
//        //fixme: enroll was moved to courseEnrollmentsService
//    }

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
        mockUser.purchaseCourse(mockCourse);
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
    public void disableCourse_shouldDisableCourse() {
        User initiator = Helpers.createMockTeacher();
        Course course = Helpers.createMockCourse(initiator);
        course.setEnabled(true);

        courseService.disableCourse(course, initiator);

        Assertions.assertFalse(course.isEnabled());
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
    public void enableCourse_shouldEnableCourse() {
        User initiator = Helpers.createMockTeacher();
        Course course = Helpers.createMockCourse(initiator);
        course.setEnabled(false);

        courseService.enableCourse(course, initiator);
        Assertions.assertTrue(course.isEnabled());
    }

    @Test
    public void delete_shouldThrowException_when_initiatorIsNot_teacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseService.delete(mockCourse, mockUser));
    }

    @Test
    public void delete_shouldCallRepository_whenInitiatorIsCreator() {
        User courseCreator = Helpers.createMockTeacher();
        Course courseToDelete = Helpers.createMockCourse(courseCreator);

        courseService.delete(courseToDelete, courseCreator);

        Mockito.verify(courseRepository,Mockito.times(1)).delete(courseToDelete);
    }

    @Test
    public void update_shouldThrowException_when_TitleAlreadyExists() {
        User mockTeacher = Helpers.createMockTeacher();
        Course mockToUpdate = Helpers.createMockCourse();
        Course existingCourse =Helpers.createMockCourse();
        existingCourse.setTitle("javaTron");
        CourseModel courseModel = Helpers.createMockCourseModel("javaTron");

        Mockito.when(courseRepository.findByTitle(courseModel.getTitle())).thenReturn(Optional.of(existingCourse));
        Assertions.assertThrows(DuplicateEntityException.class, () -> courseService.update(courseModel,mockToUpdate,mockTeacher));
    }

    @Test
    public void update_shouldThrowException_whenInitiatorIsNotTeacherOrAdminOrCreator() {
        User initiator = Helpers.createMockUser();
        Course courseToUpdate = Helpers.createMockCourse();
        CourseModel courseModel = Helpers.createMockCourseModel("wakanda");

        Assertions.assertThrows(UnauthorizedOperationException.class,  () -> courseService.update(courseModel, courseToUpdate, initiator));
    }

    @Test
    public void update_shouldCallRepository_whenInitiator_isAuthorized() throws ParseException {
        User initiator = Helpers.createMockUser(21);
        Course courseToUpdate = Helpers.createMockCourse(initiator);
        CourseModel courseModel = Helpers.createMockCourseModel("wakanda");

        courseService.update(courseModel, courseToUpdate, initiator);

        Mockito.verify(courseRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void create_shouldThrowException_whenInitiatorIsNot_teacherOrAdmin() {
        User initiator = Helpers.createMockUser(12);
        CourseModel mockCourse = Helpers.createMockCourseModel("java_lol");

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseService.create(new CourseModel(), initiator));
    }

    @Test
    public void create_shouldCallRepository_when_TitleIsUnique() {
        User initiator = Helpers.createMockTeacher();
        CourseModel courseModel = Helpers.createMockCourseModel("java_101");

        courseService.create(courseModel, initiator);
        Mockito.verify(courseRepository, Mockito.times(1)).save(Mockito.any(Course.class));
    }

    @Test
    public void create_should_throwException_when_titleAlreadyExists() {
        User mockUser = Helpers.createMockTeacher();
        CourseModel mockCourseModel =  Helpers.createMockCourseModel("Eloquent js");
        Course mockExistingCourse = Helpers.createMockCourse();
        mockExistingCourse.setTitle("Eloquent js");

        Mockito.when(courseRepository.findByTitle(mockCourseModel.getTitle())).thenReturn(Optional.of(mockExistingCourse));

        Assertions.assertThrows(DuplicateEntityException.class, () -> courseService.create(mockCourseModel, mockUser));
    }

    @Test
    public void mapAllToModel_shouldReturn_courseModelList() {
        User mockUser = Helpers.createMockUser();
        List<Course> courseList = Helpers.createMockCourseList();

        Mockito.when(ratingService.getAverageRatingForCourse(Mockito.any())).thenReturn(Mockito.any(Double.class));
        List<CourseModel> resultList = courseService.mapAllToModel(courseList, mockUser, true);
        Assertions.assertEquals(courseList.get(0).getId(), resultList.get(0).getId());
    }

    @Test
    public void titleAlreadyExists_shouldReturnFalse_when_newAndOldTitles_areTheSame() {
        Course mockCourse = Helpers.createMockCourse();
        String newTitle = mockCourse.getTitle();

        Assertions.assertFalse(courseService.titleAlreadyExists(mockCourse.getTitle(), newTitle));
    }

    @Test
    public void purchaseShouldThrowException_when_UserIsAlreadyEnrolledToCourse() {
        User mockUser = Helpers.createMockUser(21);
        Course course = Helpers.createMockCourse();

        mockUser.purchaseCourse(course);
        Assertions.assertThrows(DuplicateEntityException.class, () -> courseService.mint(mockUser, course));
    }

    @Test
    public void purchase_shouldCallAllOtherServiceMethods() {
        User mockUser = Helpers.createMockUser(21);
        Course mockCourse = Helpers.createMockCourse();

        Mockito.doNothing().when(walletService).purchaseCourse(mockCourse, mockUser);
        Mockito.doNothing().when(transactionService).create(Mockito.any(Transaction.class), Mockito.any());
        Mockito.doNothing().when(enrollmentService).purchase(mockUser, mockCourse);
        courseService.mint(mockUser, mockCourse);

        Mockito.verify(walletService, Mockito.times(1)).purchaseCourse(mockCourse, mockUser);
        Mockito.verify(transactionService, Mockito.times(1)).create(Mockito.any(), Mockito.any());
        Mockito.verify(enrollmentService, Mockito.times(1)).purchase(mockUser, mockCourse);
    }

    @Test
    void complete_shouldCallUserRepository_andUpdateUserCompletedCourses() {
        User mockUser = Helpers.createMockUser(21);
        Course mockCourse = Helpers.createMockCourse();

        mockUser.purchaseCourse(mockCourse);

        courseService.complete(mockCourse, mockUser);

        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
    }

    @Test
    public void upload_ShouldThrowException_whenInitiator_isNotCreatorNeitherAdmin() throws IOException {
        User initiator = Helpers.createMockUser(21);
        Course mockCourse = Helpers.createMockCourse();

        Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));

        Assertions.assertThrows(UnauthorizedOperationException.class,() -> courseService.upload(Mockito.any(),1, initiator));
    }

    @Test
    public void upload_ShouldCallRepository_and_updateEntity() throws IOException {
        User initiator = Helpers.createMockTeacher();
        Course courseToUpdate = Helpers.createMockCourse(initiator);

        Mockito.when(courseRepository.findById(courseToUpdate.getId())).thenReturn(Optional.of(courseToUpdate));

        courseService.upload(Mockito.any(), courseToUpdate.getId(), initiator);

        Mockito.verify(courseRepository, Mockito.times(1)).save(courseToUpdate);
    }

    @Test
    public void getTopThreeCoursesByRating_ShouldReturnEntityList() {

        Mockito.when(courseRepository.getThreeRandomCourses()).thenReturn(Helpers.createMockCourseList());

        List<CourseModel> courseList = courseService.getTopTheeCoursesByRating();
        Assertions.assertFalse(courseList.isEmpty());
    }

    @Test
    public void addLectureToCourse_shouldThrowException_whenInitiatorIsNotCourseCreator() {
        User initiator = Helpers.createMockUser();
        Course mockCourse = Helpers.createMockCourse(Helpers.createMockTeacher());
        Lecture lectureToAdd = Helpers.createMockLecture(mockCourse);

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> courseService.addLectureToCourse(lectureToAdd, mockCourse, initiator));
    }

    @Test
    public void addLectureToCourse_shouldAddLectureToCourse() {
        User initiator = Helpers.createMockTeacher();
        Course mockCourse = Helpers.createMockCourse(initiator);
        mockCourse.setCourseLectures(new ArrayList<>());
        Lecture mockLecture = Helpers.createMockLecture(mockCourse);

        courseService.addLectureToCourse(mockLecture, mockCourse, initiator);

        Assertions.assertEquals(mockCourse.getCourseLectures().get(0).getId(), mockLecture.getId());
    }

    //todo: add tests for
    // purhcase method
    // getAllByCreator

}
