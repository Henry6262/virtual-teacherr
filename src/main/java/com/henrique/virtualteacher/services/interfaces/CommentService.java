package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CommentModel;
import com.henrique.virtualteacher.models.CourseModel;

import java.util.Set;

public interface CommentService {

    CommentModel getById(int id);

    Set<CommentModel> getAllUserCourseComments(int courseId, int userId);

    Set<CommentModel> getAllForUser(int userId, User loggedUser);

    Set<CommentModel> getAllForCourse(int courseId);

    void create(User creator, Course course, String comment);

    void delete(int commentId, User loggedUser);

    void update(int commentId,String newComment, User loggedUser);

}
