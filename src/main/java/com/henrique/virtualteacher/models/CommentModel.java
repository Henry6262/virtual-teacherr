package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class CommentModel {

    public CommentModel(Comment comment) {
        this.userId = comment.getUser().getId();
        this.content = comment.getContent();
        this.courseId = comment.getCourse().getId();

    }

    @NotBlank
    private int userId;

    @NotBlank
    private int courseId;

    @NotBlank
    private String content;

}
