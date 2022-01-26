package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Lecture;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Integer> {

    Optional<Lecture> getById(int id);

    Optional<Lecture> findByEntryIdAndCourseId(int entryId, int courseId);

    void deleteAllByCourseId(int courseId);

    Optional<Lecture> getByTitle(String title);

    List<Lecture> findAll();

    List<Lecture> getAllByCourseId(int id);

    List<Lecture> getAllByEnabled(boolean enabled);


}
