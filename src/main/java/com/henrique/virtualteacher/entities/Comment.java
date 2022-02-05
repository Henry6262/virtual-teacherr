package com.henrique.virtualteacher.entities;


import lombok.AllArgsConstructor;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

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
