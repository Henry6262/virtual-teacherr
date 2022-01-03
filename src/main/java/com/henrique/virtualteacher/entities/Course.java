package com.henrique.virtualteacher.entities;

import com.henrique.virtualteacher.models.EnumTopics;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor

@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "title")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic")
    private EnumTopics topic;

    @Column(name = "description")
    private String description;

    @Column(name = "difficulty")
    private int difficulty;

    @Column(name = "starting_date")
    @DateTimeFormat(pattern = "dd.mm.yyyy")
    private Date startingDate;

    @Column(name = "enabled")
    private boolean enabled;

    @OneToMany
    @JoinTable(name = "course_ratings",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<CourseRating> ratings;

    @OneToMany
    @JoinTable(name = "comments",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<Comment> comments;

}
