package com.henrique.virtualteacher.repositories;

import com.henrique.virtualteacher.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Optional<Comment> getById(int id);

    Set<Comment> getAllByCourseIdAndUserId(int courseId, int userId);

    Set<Comment> getAllByUserId(int userId);

    Set<Comment> getAllByCourseId(int courseId);

}
