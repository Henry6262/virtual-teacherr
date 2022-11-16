package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Comment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CommentModel;
import com.henrique.virtualteacher.repositories.CommentRepository;
import com.henrique.virtualteacher.services.interfaces.CommentService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public CommentModel getById(int id) {
        Comment comment =  commentRepository.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment", "Id", String.valueOf(id)));

        return new CommentModel(comment);
    }

    private CommentModel getModelByObject(Comment comment) {
        return new CommentModel(comment);
    }

    private Set<CommentModel> getModelList(Collection<Comment> comments) {
        Set<CommentModel> commentModels = new HashSet<>();

        for (Comment current: comments) {
            CommentModel commentModel = new CommentModel(current);
            commentModels.add(commentModel);
        }
        return commentModels;
    }
    @Override
    public Set<CommentModel> getAllUserCourseComments(int courseId, int userId) {

        Set<Comment> comments = commentRepository.getAllByCourseIdAndUserId(courseId, userId);
        return getModelList(comments);
    }

    @Override
    public Set<CommentModel> getAllForUser(int userId, User loggedUser) {
        Set<Comment> comments = commentRepository.getAllByUserId( userId);
        return getModelList(comments);
    }

    @Override
    public Set<CommentModel> getAllForCourse(int courseId) {
        Set<Comment> comments = commentRepository.getAllByCourseId(courseId);
        return getModelList(comments);
    }

    @Override
    public void create(User creator, Course course, String comment) {
        commentRepository.save(new Comment(creator, course, comment));
    }

    @Override
    public void delete(int commentId, User loggedUser) {
        Comment comment = commentRepository.getById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", "id", String.valueOf(commentId)));

        checkUserIsAuthorized(comment, loggedUser);

        commentRepository.delete(comment);
    }

    private void checkUserIsAuthorized(Comment comment, User loggedUser) {
        if (loggedUser.isNotTeacherOrAdmin() && comment.getUser().getId() != loggedUser.getId()){
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to delete Comment with id: {%d}", loggedUser.getId(), comment.getId()));
        }
    }

    @Override
    public void update(int commentId, String newComment, User loggedUser) {
        Comment comment = commentRepository.getById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", "id", String.valueOf(commentId)));

        checkUserIsAuthorized(comment, loggedUser);

        comment.setContent(newComment);
        commentRepository.save(comment);
    }

}
