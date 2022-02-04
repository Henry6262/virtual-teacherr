package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.models.EnumTopics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface CourseRepository extends JpaRepository<Course, Integer> {

    Optional<Course> findById(int id);

    Optional<Course> findByTitle(String title);

    List<Course> findByEnabled(boolean isEnabled);

    List<Course> findByTopic(EnumTopics topic);

    List<Course> findByDifficulty(int difficultyLevel);
}
