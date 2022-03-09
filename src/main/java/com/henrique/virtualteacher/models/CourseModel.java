package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CourseModel {

    private int id;

    @NotBlank
    private String title;

    @NotBlank
    private EnumTopics topic;

    @NotBlank
    private String description;

    private EnumDifficulty difficulty;

    @NotBlank
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startingDate;

    private String picture;

    @NotBlank
    private double averageRating;

    private int courseCompletionPercentage;

    private String skill1;
    private String skill2;
    private String skill3;

    private Set<CommentModel> comments;
}
