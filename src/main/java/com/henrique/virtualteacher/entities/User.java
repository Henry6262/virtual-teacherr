package com.henrique.virtualteacher.entities;


import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.models.EnumRoles;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.*;
import java.util.stream.Collectors;

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

    @OneToOne
    @JoinTable(name = "wallets",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private Wallet wallet;

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
    @JoinTable(name = "users_completed_lectures",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "lecture_id"))
    private Set<Lecture> completedLectures;

//    @OneToMany()
//    @JoinTable(name = "users_completed_courses",
//        joinColumns = @JoinColumn(name = "user_id"),
//        inverseJoinColumns = @JoinColumn(name = "course_id")) //fixme -> this fixed my problem when adding
//    private List<Course> completedCourses;
//
//    @ManyToMany()
//    @JoinTable(name = "users_enrolled_courses",
//        joinColumns = @JoinColumn(name = "user_id") ,
//        inverseJoinColumns = @JoinColumn(name = "course_id"))
//    private List<Course> enrolledCourses;

    @OneToMany
    @JoinTable(name = "course_enrollments",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<CourseEnrollment> courseEnrollments;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "assignments",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "assignment_id"))
    private List<Assignment> assignments;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "comments",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<Comment> comments;

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public List<Course> getCompletedCourses() {
        return courseEnrollments.stream()
                .filter(CourseEnrollment::isCompleted)
                .map(CourseEnrollment::getCourse)
                .collect(Collectors.toList());
    }

    public List<Course> getEnrolledCourses() {
        return courseEnrollments.stream()
                .map(CourseEnrollment::getCourse)
                .collect(Collectors.toList());
    }

    public void enrollToCourse(Course course) {
        if (isEnrolledInCourse(course)){
            throw new ImpossibleOperationException(String.format("User with id: {%d}, Is already enrolled in course with id: {%d}", this.getId(), course.getId()));
        } else if (hasCompletedCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id {%d}, has already completed Course with id {%d}", this.getId(), course.getId()));
        }
        CourseEnrollment newCourseEnrollment = new CourseEnrollment(this, course);
        courseEnrollments.add(newCourseEnrollment);  //fixme: change made here
    }

    public void completeLecture(Lecture lecture) {
        if (!isEnrolledInCourse(lecture.getCourse())) {
            throw new ImpossibleOperationException(String.format("User with id: {%d}, is not enrolled in Course with id: {%d}", this.getId(), lecture.getCourse().getId()));
        }
        if (hasCompletedLecture(lecture)) {
            throw new ImpossibleOperationException(String.format("Lecture with id:{%d}, has already been completed", lecture.getId()));
        }
        completedLectures.add(lecture);
    }

    public void completeCourse(Course course) {

        if (!isEnrolledInCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d},cannot complete Course with id: {%d}, because he is not enrolled", this.getId(), course.getId()));
        }

        if (hasCompletedCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already completed Course with id: {%d}", this.getId(), course.getId()));
        }

//        completedCourses.add(course); //fixme change made here
        CourseEnrollment toComplete = courseEnrollments.stream()
                .filter(c -> c.getCourse().getId() == course.getId())
                .collect(Collectors.toList()).get(0);

        if (toComplete == null) {
            throw new EntityNotFoundException(String.format("User with id: %d, in not enrolled to course with id: %d", this.getId(), course.getId()));
        }

        toComplete.setCompleted(true);
    }

    public void addAssignment(Assignment assignment) {
        if (hasAssignment(assignment)) {
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already submitted and assignment for lecture with id: {%d}", this.getId(), assignment.getLecture().getId()));
        }
        assignments.add(assignment);
    }

    public boolean hasAssignment(Assignment assignment) {
        return getAssignments().stream()
                .anyMatch(assignment1 -> assignment1.getLecture().getId() == assignment.getLecture().getId());
    }

    public boolean hasCompletedCourse(Course course) {
        return this.getCourseEnrollments().stream()
                .filter(CourseEnrollment::isCompleted)
                .anyMatch(courseEnrollment -> courseEnrollment.getCourse().getId() == course.getId());
    }

    public boolean hasCompletedLecture(Lecture lecture) {
        return getCompletedLectures().stream()
                .anyMatch(l -> l.getId() == lecture.getId());
    }

    public boolean isEnrolledInCourse(Course course) {
        return this.getCourseEnrollments().stream()
                .anyMatch(c -> c.getCourse().getId() == course.getId());
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
        return !this.isTeacher() && !this.isAdmin();
    }

}
