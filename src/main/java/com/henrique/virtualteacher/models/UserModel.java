package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.*;
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

    public UserModel(User user) {
        setEmail(user.getEmail()) ;
        setFirstname(user.getFirstName());
        setLastname(user.getLastName());
        setProfilePicture(user.getProfilePicture());
        setRolesList(user.getRoles());
        setCompletedLectures(user.getCompletedLectures());
        setCompletedCourses(user.getCompletedCourses());
        setOwnedNftCourses(user.getNftCourses());
        setAssignments(user.getAssignments());
        setComments(user.getComments());
    }

    @NotBlank
    private String email;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;

    private String profilePicture;

    private int walledId;

    @NotBlank
    private List<Role> rolesList;

    @NotBlank
    private Set<Lecture> completedLectures;

    @NotBlank
    private List<NFT> ownedNftCourses;

    @NotBlank
    private List<Assignment> assignments;

    private List<Course> completedCourses;

    private List<Comment> comments;


}
