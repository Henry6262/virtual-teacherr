package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    Optional<Rating> findById(int id);

    List<Rating> getAllByCourseId(int id);

    List<Rating> getAllByUserId(int id);



}
