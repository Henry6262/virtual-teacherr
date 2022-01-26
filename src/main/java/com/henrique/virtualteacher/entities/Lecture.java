package com.henrique.virtualteacher.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;


import javax.persistence.*;
import java.util.List;

@Entity()
@Getter
@Setter
@NoArgsConstructor
@Table(name = "lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private int id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "entry_id")
    private int entryId;

    //todo : fix

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "video")
    private String videoLink;

    @Column(name = "assignment")
    private String assignmentText;     //fixme: maybe try to do it with textFile

    @Column(name = "enabled")
    private boolean enabled;

    @ManyToMany()
    @JsonIgnore
    @JoinTable(name = "users_completed_lectures",
    joinColumns = @JoinColumn(name = "lecture_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> usersCompleted;

}
