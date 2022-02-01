package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserModel {

    @NotBlank
    private String email;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;

    private String profilePicture;

    @NotBlank
    private List<Role> rolesList;

    @NotBlank
    private Set<Lecture> completedLectures;

    @NotBlank
    private List<Course> completedCourses;

    @NotBlank
    private List<Course> enrolledCourses;

    @NotBlank
    private List<Assignment> assignments;

}
