package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CourseModel {

    @NotNull
    private String title;

    @NotNull
    private EnumTopics topic;

    @NotNull
    private String description;

    @NotNull
    private int difficulty;

    @NotNull
    private Date startingDate;

    //enabled not necessary
}
