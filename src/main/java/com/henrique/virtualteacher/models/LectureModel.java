package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.Lecture;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class LectureModel {

    public LectureModel(Lecture lecture) {
        this.title = lecture.getTitle();
        this.description = lecture.getDescription();
        this.videoLink = lecture.getVideoLink();
        this.assignmentText = lecture.getAssignmentText();
    }

    @NotNull
    private String title;

    private String description;

    @NotNull
    private String videoLink;

    @NotNull
    private String assignmentText;

    //todo: add lists

}
