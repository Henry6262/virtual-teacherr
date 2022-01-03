package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.models.EnumTopics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    Optional<Course> findById(int id);

    Optional<Course> findByTitle(String title);

    Optional<List<Course>> findByEnabled(boolean isEnabled);

    Optional<List<Course>> findByTopic(EnumTopics topic);

    Optional<List<Course>> findByDifficulty(int difficultyLevel);
}
