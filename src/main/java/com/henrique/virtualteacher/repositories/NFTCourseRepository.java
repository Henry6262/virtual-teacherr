package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.NFT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NFTCourseRepository extends JpaRepository<NFT, Integer> {

    Optional<NFT> getById(int id);

    Optional<NFT> getByOwnerIdAndCourseId(int userId, int courseId);

    Optional<NFT>  findTopByOrderByIdDesc();


    List<NFT> getAllByOwnerId(int userId);

    List<NFT> getAllByCourseId(int id);


    List<NFT> getAllByOwnerIdAndCompleted(int userId, boolean completed);

    List<NFT> getAllByCourseIdAndCompleted(int courseId, boolean completed);

    @Query(value = "SELECT * from nft_courses n where course_id = ? and minted = ?", nativeQuery = true)
    List<NFT> getAllNonMintedFromCourse(int courseId, boolean isMinted);


}
