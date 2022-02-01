package com.henrique.virtualteacher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.models.Status;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private int id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "grade")
    private int grade;

    public void grade(int grade) {
        if (isGraded()) {
            throw new ImpossibleOperationException(String.format("Assignment with id: {%d}, is already graded", this.getId()));
        }
        setStatus(Status.GRADED);
        setGrade(grade);
    }

    public boolean isGraded() {
        return this.status.equals(Status.GRADED);
    }

}
