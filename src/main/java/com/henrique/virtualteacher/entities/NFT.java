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
@Table(name = "nft_courses")
public class NFT {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "completed")
    private boolean completed;

//    @Column(name = "minted")
//    private boolean minted;

    @Column(name = "drop_number")
    private int dropNumber;

    public NFT(User owner, Course courseToPurchase) {
        this.owner = owner;
        this.course = courseToPurchase;
        this.completed = false;
        this.dropNumber = setDropNumber(course);
    }

    private int setDropNumber(Course course) {
        if (course.getNfts().size() == 0) {
            return 1;
        } else {
            return course.getNfts().get(course.getNfts().size() -1).getDropNumber()
                    + 1;
        }
    }

}
