package com.henrique.virtualteacher.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "content")
    private String content;

    public Comment(User creator, Course course, String comment) {
        this.user = creator;
        this.course = course;
        this.content = comment;
    }
}
