package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<CourseRating, Integer> {

    Optional<CourseRating> findById(int id);

    List<CourseRating> getAllByCourseId(int id);

    List<CourseRating> getAllByUserId(int id);



}
