package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class LectureModel {

    @NotNull
    private String title;

    private String description;

    @NotNull
    private String videoLink;

    @NotNull
    private String assignmentText;

}
