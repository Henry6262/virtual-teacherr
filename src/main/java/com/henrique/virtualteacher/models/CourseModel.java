package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.Course;
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

    public CourseModel(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.topic = course.getTopic();
        this.description = course.getDescription();
        this.difficulty = course.getDifficulty();
        this.startingDate = course.getStartingDate();
        this.picture = course.getPicture();
        this.creatorEmail = course.getCreator().getEmail();
        this.price = course.getPrice();
        this.skill1 = course.getSkill1();
        this.skill2 = course.getSkill2();
        this.skill3 = course.getSkill3();
    }

    private int id;


    private String title;

    private EnumTopic topic;

    private String description;

    private EnumDifficulty difficulty;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startingDate;

    private String picture;

    private String creatorEmail;

    private double averageRating;

    private BigDecimal price;

    private int availableMints;

    private int courseCompletionPercentage;

    private String skill1;
    private String skill2;
    private String skill3;

    private Set<CommentModel> comments;
}
