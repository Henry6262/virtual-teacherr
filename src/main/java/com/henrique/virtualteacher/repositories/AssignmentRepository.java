package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    @Override
    Optional<Assignment> findById(Integer integer);

    Optional<Assignment> getByUserIdAndLectureId(int userId, int lectureId) ;

    List<Assignment> getAllByUserIdAndLectureCourseId(int userId, int courseId);

    List<Assignment> getAllByLectureCourseId(int courseId);

    List<Assignment> getAllByLectureCourseIdAndStatus(int courseId, Status status);

    List<Assignment> getAllByStatus(Status status);

    List<Assignment> getAllByUserIdAndLectureCourseIdAndStatus(int userId, int courseId, Status status);

}


//todo: only will be able to complete lecture if assignment is graded by  a teacher