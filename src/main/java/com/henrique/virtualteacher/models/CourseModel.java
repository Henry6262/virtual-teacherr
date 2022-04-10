package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
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
    private EnumTopic topic;

    @NotBlank
    private String description;

    private EnumDifficulty difficulty;

    @NotBlank
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startingDate;

    private String picture;

    @NotBlank
    private String creatorEmail;

    @NotBlank
    private double averageRating;

    @NotBlank
    private BigDecimal price;

    private int courseCompletionPercentage;

    private String skill1;
    private String skill2;
    private String skill3;

    private Set<CommentModel> comments;
}
