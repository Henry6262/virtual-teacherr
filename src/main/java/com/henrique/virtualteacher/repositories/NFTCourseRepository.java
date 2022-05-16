package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.NFTCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NFTCourseRepository extends JpaRepository<NFTCourse, Integer> {

    Optional<NFTCourse> getById(int id);

    Optional<NFTCourse> getByOwnerIdAndCourseId(int userId, int courseId);

    List<NFTCourse> getAllByOwnerId(int userId);

    List<NFTCourse> getAllByCourseId(int id);

    List<NFTCourse> getAllByCourseIdAndMinted(int courseId, boolean isMinted);

    List<NFTCourse> getAllByOwnerIdAndCompleted(int userId, boolean completed);

    List<NFTCourse> getAllByCourseIdAndCompleted(int courseId, boolean completed);

    @Query(value = "SELECT * from nft_courses n where course_id = ? and minted = ?", nativeQuery = true)
    List<NFTCourse> getAllNonMintedFromCourse(int courseId, boolean isMinted);


}
