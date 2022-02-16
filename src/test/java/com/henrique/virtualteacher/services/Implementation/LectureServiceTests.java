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

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LectureServiceTests {

    @Mock
    LectureRepository lectureRepository;
    @Mock
    ModelMapper modelMapper;

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
        User  mockUser = Helpers.createMockTeacher();
        Lecture lecture = Helpers.createMockLecture();

        Assertions.assertThrows(EntityNotFoundException.class, () -> lectureService.delete(lecture, mockUser));
    }

    @Test
    public void deleteAllByCourseId_shouldThrowException_whenUserIsNot_teacherOrAdmin() {
        Course mockCourse = Helpers.createMockCourse();
        User mockUser = Helpers.createMockUser();

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> lectureService.deleteAllByCourseId(mockCourse.getId(), mockUser));
    }


}
