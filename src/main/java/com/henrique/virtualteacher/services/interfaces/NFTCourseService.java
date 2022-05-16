package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.NFTCourse;
import com.henrique.virtualteacher.entities.User;

import java.util.List;

public interface NFTCourseService {

    NFTCourse getById(int id);

    List<NFTCourse> getAllForUser(User loggedUser, int userToGetId);

    List<NFTCourse> getAllForCourse(User loggedUser, int courseToGetId);

    List<NFTCourse> getAllForUser(User loggedUser, int userToGetId, boolean completed);

    List<NFTCourse> getAllForCourse(User loggedUser, int courseToGet, boolean completed);

    List<NFTCourse> getAllForCourseByMinted(User loggedUser, int courseToGet, boolean minted);

    NFTCourse getUserOwnedNFTCourse(User loggedUser, Course enrolledCourse);

    void checkCourseHasAvailableMints(User loggedUser, int courseId);

    void createCourseNFTItems(Course course, User loggedUser);

    NFTCourse purchase(User purchaser, Course toMint);

    void leave(User leavingUser, Course courseToLeave);

}
