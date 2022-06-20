package com.henrique.virtualteacher.services.interfaces;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.NFT;
import com.henrique.virtualteacher.entities.User;

import java.util.List;

public interface NFTCourseService {

    NFT getById(int id);

    List<NFT> getAllForUser(User loggedUser, int userToGetId);

    List<NFT> getAllForCourse(User loggedUser, int courseToGetId);

    List<NFT> getAllForUser(User loggedUser, int userToGetId, boolean completed);

    List<NFT> getAllForCourse(User loggedUser, int courseToGet, boolean completed);

    List<NFT> getAllForCourseByMinted(User loggedUser, int courseToGet, boolean minted);

    NFT getUserOwnedNFTCourse(User loggedUser, Course enrolledCourse);

    void checkCourseHasAvailableMints(User loggedUser, int courseId);

    void createCourseNFTItems(Course course, User loggedUser);

    NFT purchase(User purchaser, Course toMint);

    void leave(User leavingUser, Course courseToLeave);

}
