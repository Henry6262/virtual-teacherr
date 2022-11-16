package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.LectureModel;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface LectureService {


    List<LectureModel> mapAllToModel(List<Lecture> lectures);
    Lecture getById(int id);

    Lecture getByEntryIdAndCourseId(int entryId, int courseId);

    Lecture getByTitle(String title);

    List<Lecture> getAll();

    List<Lecture> getAllByCourseId(int id);

    List<Lecture> getAllByEnabled(boolean enabled);

    void completeLectureForUser(User loggedUser, Lecture lecture);

    Lecture create(Lecture lecture, User loggedUser);

    void update(LectureModel lectureModel, Lecture lecture, User loggedUser);

    void delete(Lecture lecture, User loggedUser);

    void deleteAllByCourseId(int courseId, User loggedUser);

    Lecture mapModelToEntity(LectureModel lectureModel, Course course);




}
