package com.henrique.virtualteacher.services.implementation;

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
import com.henrique.virtualteacher.services.interfaces.LectureService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@AllArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Lecture getById(int id) {
        return lectureRepository.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lecture", "id", String.valueOf(id)));
    }

    @Override
    public Lecture getByEntryIdAndCourseId(int entryId, int courseId) {
        return lectureRepository.findByEntryIdAndCourseId(entryId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Lecture", "courseId and entryId", String.format("%d, %d",courseId, entryId)));
    }

    @Override
    public Lecture getByTitle(String title) {
        return lectureRepository.getByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("Lecture", "Title", title));
    }

    @Override
    public List<Lecture> getAll() {
        return lectureRepository.findAll();
    }

    @Override
    public List<Lecture> getAllByCourseId(int id) {
        return lectureRepository.getAllByCourseId(id);
    }

    @Override
    public List<Lecture> getAllByEnabled(boolean enabled) {
        return lectureRepository.getAllByEnabled(enabled);
    }

    @Override
    public void completeLectureForUser(User loggedUser, Lecture lecture) {

        if (!loggedUser.isEnrolledInCourse(lecture.getCourse())) {
            throw new ImpossibleOperationException(String.format("User with id: %d, is not enrolled into course with id %d", loggedUser.getId(), lecture.getCourse().getId()));
        }

        loggedUser.completeLecture(lecture);
        userRepository.save(loggedUser);
    }

    @Override
    public Lecture create(Lecture lecture, User loggedUser) {

        User courseCreator = lecture.getCourse().getCreator();

        if (loggedUser.getId() != courseCreator.getId() && !loggedUser.isAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to create lectures", loggedUser.getId()));

        }
        checkIfTitleIsUnique(lecture.getTitle());

        return lectureRepository.save(lecture);
    }

    public Lecture mapModelToEntity(LectureModel lectureModel, Course course) {
        Lecture lecture = new Lecture();
        lecture.setTitle(lectureModel.getTitle());
        lecture.setTitle(lectureModel.getTitle());
        lecture.setDescription(lectureModel.getDescription());
        lecture.setAssignmentText(lectureModel.getAssignmentText());
        lecture.setVideoLink(lectureModel.getVideoLink());
        lecture.setEntryId(course.getCourseLectures().size() +1);
        lecture.setCourse(course);
        lecture.setEnabled(false);
        return lecture;
    }

    private void checkIfTitleIsUnique(String title) {

        try {
            Lecture lecture = lectureRepository.getByTitle(title)
                    .orElseThrow(() -> new EntityNotFoundException("Lecture","Title", title));
        } catch (EntityNotFoundException e) {
            return;
        }
        throw new DuplicateEntityException("Lecture","Title", title);
    }

    @Override
    public void update(LectureModel lectureModel, Lecture lecture, User loggedUser) {

        if (!lectureModel.getTitle().equals(lecture.getTitle())){
            checkIfTitleIsUnique(lectureModel.getTitle());
        }

        modelMapper.map(lectureModel, lecture);
        lectureRepository.save(lecture);
    }

    @Override
    public void delete(Lecture lecture, User loggedUser) {

        User courseCreator = lecture.getCourse().getCreator();
        if (loggedUser.getId() != courseCreator.getId() && !loggedUser.isAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with the id: {%d}, does not have permission to delete Lecture with id: {%d}", loggedUser.getId(), lecture.getId()));
        }

        orderCourseLectures(lecture);

        lecture.getUsersCompleted().clear();
        lectureRepository.delete(lecture);
    }

    private void orderCourseLectures(Lecture lecture) {

        int entryId = lecture.getEntryId();
        List<Lecture> courseLectures = lecture.getCourse().getCourseLectures();

        for (int index = entryId +1 ; index <= courseLectures.size() ; index++) {

            Lecture currentLecture = getByEntryIdAndCourseId(index, lecture.getCourse().getId());
            currentLecture.setEntryId(currentLecture.getEntryId() -1);
        }

    }

    @Override
    @Transactional
    public void deleteAllByCourseId(int courseId, User loggedUser) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("User", "id", String.valueOf(loggedUser.getId()), "delete", "Lectures", "courseId", String.valueOf(courseId));
        }
        lectureRepository.deleteAllByCourseId(courseId);
    }
}
