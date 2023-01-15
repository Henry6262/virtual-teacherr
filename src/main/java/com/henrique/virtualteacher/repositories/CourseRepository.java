package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.models.EnumDifficulty;
import com.henrique.virtualteacher.models.EnumTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> getCoursesByCreatorId(int creatorId);

    Optional<Course> findById(int id);

    Optional<Course> findByTitle(String title);

    List<Course> findByEnabled(boolean isEnabled);

    List<Course> findByTopic(EnumTopic topic);

    List<Course> findByDifficulty(EnumDifficulty difficulty);

    @Query(value = "select * from courses c order by c.title asc limit 3", nativeQuery = true)
    List<Course> getThreeRandomCourses();

}
