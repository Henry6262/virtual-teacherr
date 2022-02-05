package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class CommentModel {

    @NotBlank
    private int userId;

    @NotBlank
    private int courseId;

    @NotBlank
    private String content;

}
