package com.henrique.virtualteacher.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany()
    @JoinTable(name = "users_completed_courses",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<Course> completedCourses;

    @ManyToMany()
    @JoinTable(name = "users_enrolled_courses",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> enrolledCourses;

    public boolean enrollToCourse(Course course) {
        if (!enrolledCourses.contains(course)){
            enrolledCourses.add(course);
            return true;
        }
        return false;
    }

}
