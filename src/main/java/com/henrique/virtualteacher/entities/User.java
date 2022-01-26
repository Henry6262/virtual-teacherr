package com.henrique.virtualteacher.entities;


import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.models.EnumRoles;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.List;

@Getter
@Setter
@Entity

@NoArgsConstructor

@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "enabled")
    private boolean enabled;

    @ManyToMany()
    @JoinTable(name = "users_completed_lectures",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "lecture_id"))
    private List<Lecture> completedLectures;

    @OneToMany()
    @JoinTable(name = "users_completed_courses",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")) //fixme -> this fixed my problem when adding
    private List<Course> completedCourses;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "users_enrolled_courses",
        joinColumns = @JoinColumn(name = "user_id") ,
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> enrolledCourses;

    public void enrollToCourse(Course course) {
        if (enrolledCourses.contains(course)){
            throw new ImpossibleOperationException(String.format("User with id: {%d}, Is already enrolled in course with id: {%d}", this.getId(), course.getId()));
        } else if (completedCourses.contains(course)) {
            throw new ImpossibleOperationException(String.format("User with id {%d}, has already completed Course with id {%d}", this.getId(), course.getId()));
        }
        enrolledCourses.add(course);
    }

    public void completeLecture(Lecture lecture) {
        if (completedLectures.contains(lecture)) {
            throw new ImpossibleOperationException(String.format("Lecture with id:{%d}, has already been completed", lecture.getId()));
        }
        completedLectures.add(lecture);
    }

    public void completeCourse(Course course) {

        if (!enrolledCourses.contains(course)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d},cannot complete Course with id: {%d}, because he is not enrolled", this.getId(), course.getId()));
        }

        if (completedCourses.contains(course)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already completed Course with id: {%d}", this.getId(), course.getId()));
        }

        completedCourses.add(course);
    }

    public boolean hasCompletedCourse(Course course) {
        return getCompletedCourses().stream()
                .anyMatch(course1 -> course.getId() == course.getId());
    }

    public boolean isTeacher() {
        return (this.getRoles().stream()
                .anyMatch(role -> role.getRole().equals(EnumRoles.TEACHER)));
    }

    public boolean isAdmin() {
        return this.getRoles().stream()
                .anyMatch(role -> role.getRole().equals(EnumRoles.ADMIN));
    }

    public boolean isStudent() {
        return this.getRoles().stream()
                .anyMatch(role -> role.getRole().equals(EnumRoles.STUDENT));
    }

    public boolean isNotTeacherOrAdmin() {
        if (!this.isTeacher() && !this.isAdmin()) {
            return true;
        }
        return false;
    }

}
