package com.henrique.virtualteacher.services.Implementation;

import com.henrique.virtualteacher.entities.Comment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CommentModel;
import com.henrique.virtualteacher.repositories.CommentRepository;
import com.henrique.virtualteacher.services.Helpers;
import com.henrique.virtualteacher.services.implementation.CommentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTests {

    @Mock
    CommentRepository commentRepository;

    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    CommentServiceImpl commentService;

    @Test
    public void getById_shouldThrowException_when_commentDoesNotExist() {
        Comment comment = Helpers.createMockComment();

        Assertions.assertThrows(EntityNotFoundException.class, () ->commentService.getById(comment.getId()));
    }

    @Test
    public void getById_ShouldReturnEntity_when_Existing() {
        Comment comment = Helpers.createMockComment();
        Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.of(comment));

        CommentModel result = commentService.getById(comment.getId());
        Mockito.verify(commentRepository, Mockito.times(1))
                .getById(comment.getId());
    }

    @Test
    public void getAllForCourse_shouldCallRepository() {
        Comment comment = Helpers.createMockComment();
        Set<Comment> mockComments = Helpers.createMockCommentModelList();

        Mockito.when(commentRepository.getAllByCourseId(comment.getCourse().getId())).thenReturn(mockComments);

        Set<CommentModel> result = commentService.getAllForCourse(comment.getCourse().getId());

        Mockito.verify(commentRepository, Mockito.times(1))
                .getAllByCourseId(comment.getCourse().getId());
    }

    @Test
    public void getAllForUser_shouldCallRepository() {
        User mockUser = Helpers.createMockUser();
        Set<Comment> mockComments = Helpers.createMockCommentModelList();

        Mockito.when(commentRepository.getAllByUserId(mockUser.getId())).thenReturn(mockComments);

        commentService.getAllForUser(mockUser.getId(), Helpers.createMockTeacher());

        Mockito.verify(commentRepository, Mockito.times(1))
                .getAllByUserId(mockUser.getId());
    }

    @Test
    public void create_shouldCallRepository() {
        User initiator = Helpers.createMockUser();
        Course course = Helpers.createMockCourse();
        Comment comment = Helpers.createMockComment();

        commentService.create(initiator, course, "amazing ");

        Mockito.verify(commentRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void getByCourseIdAndUserId_shouldCallRepository() {
        Comment comment = Helpers.createMockComment();
        Set<Comment> mockComments = Helpers.createMockCommentModelList();

        Mockito.when(commentRepository.getAllByCourseIdAndUserId(comment.getCourse().getId(), comment.getUser().getId())).thenReturn(mockComments);

        commentService.getAllUserCourseComments(comment.getCourse().getId(), comment.getUser().getId());

        Mockito.verify(commentRepository, Mockito.times(1))
                .getAllByCourseIdAndUserId(comment.getCourse().getId(), comment.getUser().getId());

    }

    @Test
    public void update_shouldThrowException_whenUserIsNotAllowed() {
        Comment comment = Helpers.createMockComment();

        User otherMockUser = Helpers.createMockUser();
        otherMockUser.setId(123);

        Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.of(comment));

        Assertions.assertThrows(UnauthorizedOperationException.class, () ->commentService.update(comment.getId(),"yessir",otherMockUser));
    }

    @Test
    public void delete_shouldThrowException_whenUserIsNotAllowed() {
        Comment comment = Helpers.createMockComment();
        User commentCreator = comment.getUser();

        User otherUser = Helpers.createMockUser();

        Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.of(comment));

        Assertions.assertThrows(UnauthorizedOperationException.class, () -> commentService.delete(comment.getId(), otherUser));
    }

}
