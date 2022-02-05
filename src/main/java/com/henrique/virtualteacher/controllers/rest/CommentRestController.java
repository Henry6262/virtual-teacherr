package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.CommentModel;
import com.henrique.virtualteacher.services.interfaces.CommentService;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import io.swagger.models.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentRestController {

    private CommentService commentService;
    private UserService userService;
    private CourseService courseService;

    //todo: configure spring security to allow and block certain actions

    @GetMapping("/{id}")
    public ResponseEntity<CommentModel> getById(@PathVariable int id) {
        CommentModel commentModel = commentService.getById(id);
        return new ResponseEntity<>(commentModel, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<CommentModel> updateCourseComment(@PathVariable int id,
                                                            @RequestParam String newComment,
                                                            Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());

        if (newComment == null || newComment.trim().equals("")) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "comment is blank");
        }
        commentService.update(id, newComment, loggedUser);
        return new ResponseEntity<>(commentService.getById(id), HttpStatus.ACCEPTED);
    }

}
