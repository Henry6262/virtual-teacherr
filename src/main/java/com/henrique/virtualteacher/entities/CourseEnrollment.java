package com.henrique.virtualteacher.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "users_enrolled_courses")
public class CourseEnrollment {

    @Id
    @Column(name = "user_id")
    private int userId;

    @Column(name = "course_id")
    private int courseId;

    @Column(name = "completed")
    private boolean isCompleted;

}
