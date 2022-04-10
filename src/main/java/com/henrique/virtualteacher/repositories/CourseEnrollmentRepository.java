package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Integer> {

    Optional<CourseEnrollment> getById(int id);

    Optional<CourseEnrollment> getByUserIdAndCourseId(int userId, int courseId);

    List<CourseEnrollment> getAllByUserId(int userId);

    List<CourseEnrollment> getAllByCourseId(int id);

    List<CourseEnrollment> getAllByUserIdAndCompleted(int userId, boolean completed);

    List<CourseEnrollment> getAllByCourseIdAndCompleted(int courseId, boolean completed);


}
