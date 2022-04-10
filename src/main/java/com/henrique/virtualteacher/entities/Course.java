package com.henrique.virtualteacher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.models.EnumDifficulty;
import com.henrique.virtualteacher.models.EnumTopic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hibernate.annotations.CascadeType.DELETE;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Transactional
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
    private EnumTopic topic;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private EnumDifficulty difficulty;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "starting_date")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startingDate;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "picture")
    private String picture;

    @Column(name = "skill_1")
    private String skill1;

    @Column(name = "skill_2")
    private String skill2;

    @Column(name = "skill_3")
    private String skill3;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @JsonIgnore
    @OneToMany()
    @JoinTable(name = "course_enrollments",
    inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<User> enrolledUsers;

    @ManyToMany()
    @JsonIgnore
    @JoinTable(name = "users_completed_courses",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> usersCompletedCourse;

    @Cascade(value = DELETE)
    @OneToMany()
    @JoinTable(name = "lectures",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "lecture_id"))
    private List<Lecture> courseLectures;

    @ManyToMany()
    @JoinTable(name = "course_ratings",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<Rating> ratings;

    @OneToMany
    @JsonIgnore
    @JoinTable(name = "comments",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private List<Comment> comments;

    public boolean isEnabled() {
        return enabled;
    }

    public void addRating(Rating rating) {
        if (this.ratings.stream()
        .anyMatch(rating1 -> rating.getUser().getId() == rating.getUser().getId())) {
            throw new DuplicateEntityException(String.format("User with id: {%d}, has already left a rating to the course with id: {%d}", rating.getUser().getId(), rating.getId()));
        }

    }

    public void addLecture(Lecture lecture) {
        if (courseLectures.contains(lecture)) {
            throw new DuplicateEntityException(String.format("lecture with id: {%d} is already part of the course with id: {%d}",lecture.getId(), this.getId()));
        }
        courseLectures.add(lecture);
    }




}
