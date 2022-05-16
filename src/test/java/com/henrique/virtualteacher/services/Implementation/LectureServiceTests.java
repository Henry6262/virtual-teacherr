package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.LectureModel;
import com.henrique.virtualteacher.repositories.LectureRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.LectureServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LectureServiceTests {

    @Mock
    LectureRepository lectureRepository;
    @Mock
    ModelMapper modelMapper;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    LectureServiceImpl lectureService;


    @Test
    public void getById_shouldThrowException_WhenEntityNotFound() {
        Lecture lecture = Helpers.createMockLecture();

        Assertions.assertThrows(EntityNotFoundException.class, () -> lectureService.getById(21));
    }

    @Test
    public void getByEntryAndUserId_shouldThrowException_whenEntityNotFound() {
        Lecture lecture = Helpers.createMockLecture();
        Assertions.assertThrows(EntityNotFoundException.class, () -> lectureService.getByEntryIdAndCourseId(12,3));
    }

    @Test
    public void getByTitle_shouldThrowException_whenEntityNotFound() {
        Lecture lecture = Helpers.createMockLecture();
        Assertions.assertThrows(EntityNotFoundException.class, () -> lectureService.getByTitle("what does the fox say"));
    }

    @Test
    public void getAll_shouldCall_repository() {
        List<Lecture> lectures = Helpers.createMockLectureList(Helpers.createMockCourse());
        Mockito.when(lectureRepository.findAll()).thenReturn(lectures);

        lectureService.getAll();

        Mockito.verify(lectureRepository, Mockito.times(1))
                .findAll();
    }

    @Test
    public void getAllByCourseId_shouldCall_repository() {
        Course course = Helpers.createMockCourse();
        List<Lecture> lectures = Helpers.createMockLectureList(course);
        Mockito.when(lectureRepository.getAllByCourseId(course.getId())).thenReturn(lectures);

        lectureService.getAllByCourseId(course.getId());

        Mockito.verify(lectureRepository, Mockito.times(1)).getAllByCourseId(course.getId());
    }

    @Test
    public void getAllByEnabled_shouldCallRepository() {
        List<Lecture> mockLectures = Helpers.createMockLectureList(Helpers.createMockCourse());

        Mockito.when(lectureRepository.getAllByEnabled(true)).thenReturn(mockLectures);

        lectureService.getAllByEnabled(true);

        Mockito.verify(lectureRepository, Mockito.times(1))
                .getAllByEnabled(true);
    }

    @Test
    public void completeLectureForUser_shouldThrowException_whenUserIsNotEnrolledInCourse() {
        User mockUser = Helpers.createMockUser();
        Lecture lecture = Helpers.createMockLecture();

        Assertions.assertThrows(ImpossibleOperationException.class, () -> lectureService.completeLectureForUser(mockUser,lecture));
    }

    @Test
    public void create_shouldThrowException_whenTileAlreadyExists() {
        User mockTeacher = Helpers.createMockTeacher();
        Lecture lecture = Helpers.createMockLecture();

        Mockito.when(lectureRepository.getByTitle(lecture.getTitle())).thenReturn(Optional.of(lecture));

        Assertions.assertThrows(DuplicateEntityException.class, () -> lectureService.create(lecture, mockTeacher));
    }

    @Test
    public void create_shouldThrowException_whenInitiatorIsNotTeacherOrAdmin() {
        User mockUser = Helpers.createMockUser();
        Lecture mockLecture = Helpers.createMockLecture();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> lectureService.create(mockLecture, mockUser));
    }

    @Test
    public void create_shouldCallRepository_andReturnCreatedEntity() {
        User mockUser = Helpers.createMockTeacher();
        Lecture mockLecture = Helpers.createMockLecture();

        Mockito.when(lectureRepository.save(mockLecture)).thenReturn(mockLecture);

        Lecture createdLecture = lectureService.create(mockLecture, mockUser);

        Mockito.verify(lectureRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertNotNull(createdLecture);
    }

    @Test
    public void update_shouldThrowException_whenTitleAlreadyExists() {
        User mockUser = Helpers.createMockUser();
        LectureModel lectureModel = Helpers.createMockLectureModel();
        lectureModel.setTitle("akunamatata");
        Lecture lecture = Helpers.createMockLecture();

        Mockito.when(lectureRepository.getByTitle(lectureModel.getTitle())).thenReturn(Optional.of(lecture));

        Assertions.assertThrows(DuplicateEntityException.class, () -> lectureService.update(lectureModel, lecture, mockUser));
    }

    @Test
    public void delete_shouldThrowException_whenInitiator_isNotTeacherOrAdmin() {
        User  mockUser = Helpers.createMockUser();
        Lecture lecture = Helpers.createMockLecture();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> lectureService.delete(lecture, mockUser));
    }

    @Test
    public void deleteAllByCourseId_shouldThrowException_whenUserIsNot_teacherOrAdmin() {
        Course mockCourse = Helpers.createMockCourse();
        User mockUser = Helpers.createMockUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> lectureService.deleteAllByCourseId(mockCourse.getId(), mockUser));
    }

    @Test
    public void deleteAllByCourseId_shouldCallRepository_whenInitiatorIsAuthorized() {
        User initiator = Helpers.createMockAdmin();
        Course course = Helpers.createMockCourse();

        lectureService.deleteAllByCourseId(course.getId(), initiator);

        Mockito.verify(lectureRepository, Mockito.times(1)).deleteAllByCourseId(course.getId());
    }

    @Test
    public void delete_shouldClearUserCompletedLectures() {
        User initiator = Helpers.createMockAdmin();

        User user1 = Helpers.createMockUser(21);
        User user2 = Helpers.createMockUser(20);
        User user3 = Helpers.createMockUser(13);

        Course course = Helpers.createMockCourse();
        List<Lecture> courseLectures = course.getCourseLectures();
        Lecture lectureToDelete = courseLectures.get(0);


        List<User> completed = new ArrayList<>(List.of(user1,user2,user3));
        lectureToDelete.setUsersCompleted(completed);

        Mockito.when(lectureRepository.findByEntryIdAndCourseId(courseLectures.get(1).getEntryId(), course.getId())).thenReturn(Optional.of(courseLectures.get(1)));
        Mockito.when(lectureRepository.findByEntryIdAndCourseId(courseLectures.get(2).getEntryId(), course.getId())).thenReturn(Optional.of(courseLectures.get(2)));
        Mockito.when(lectureRepository.findByEntryIdAndCourseId(courseLectures.get(3).getEntryId(), course.getId())).thenReturn(Optional.of(courseLectures.get(3)));



        lectureService.delete(courseLectures.get(0), initiator);


        Assertions.assertAll(
                () -> Assertions.assertFalse(lectureToDelete.getUsersCompleted().contains(user1)),
                () -> Assertions.assertFalse(lectureToDelete.getUsersCompleted().contains(user2)),
                () -> Assertions.assertFalse(lectureToDelete.getUsersCompleted().contains(user3))
        );

    }

    @Test
    public void delete_shouldCallRepository_andClearUsersCompleted() {
        User initiator = Helpers.createMockTeacher();
        Course mockCourse = Helpers.createMockCourse(initiator);
        List<Lecture> courseLectures = Helpers.createMockLectureList(mockCourse);
        Lecture mockLecture = courseLectures.get(0);


        Mockito.when(lectureRepository.findByEntryIdAndCourseId(courseLectures.get(1).getEntryId(), mockCourse.getId())).thenReturn(Optional.of(courseLectures.get(1)));
        Mockito.when(lectureRepository.findByEntryIdAndCourseId(courseLectures.get(2).getEntryId(), mockCourse.getId())).thenReturn(Optional.of(courseLectures.get(2)));
        Mockito.when(lectureRepository.findByEntryIdAndCourseId(courseLectures.get(3).getEntryId(), mockCourse.getId())).thenReturn(Optional.of(courseLectures.get(3)));

        lectureService.delete(mockLecture, initiator);


        Mockito.verify(lectureRepository, Mockito.times(1)).delete(mockLecture);
        Assertions.assertEquals(0, mockLecture.getUsersCompleted().size());
    }

    @Test
    public void completeLectureForUser_shouldAddEntryTo_UserCompletedLectures_andCallRepo() {
        User mockUser = Helpers.createMockUser();
        Course course = Helpers.createMockCourse();
        Lecture lecture = Helpers.createMockLecture(course);
        mockUser.purchaseCourse(course);

        lectureService.completeLectureForUser(mockUser, lecture);

        Assertions.assertTrue(mockUser.getCompletedLectures().contains(lecture));
    }

    @Test
    public void mapModelToEntity_shouldReturnLectureEntity_withCorrectValues() {
        Course course = Helpers.createMockCourse();
        LectureModel lectureModel = Helpers.createMockLectureModel();

        Lecture result = lectureService.mapModelToEntity(lectureModel, course);

        Assertions.assertAll(
                () -> Assertions.assertEquals(result.getTitle(), lectureModel.getTitle()),
                () -> Assertions.assertEquals(result.getDescription(), lectureModel.getDescription()),

                () -> Assertions.assertEquals(result.getCourse().getTitle(), course.getTitle()),
                () -> Assertions.assertEquals(result.getCourse().getId(), course.getId())
        );
    }

}
