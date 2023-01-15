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


    NFT getUserOwnedNFTCourse(User loggedUser, Course enrolledCourse);

    void checkCourseHasAvailableMints(Course course);

    NFT mintNFT(User purchaser, Course toMint);

    void burnNFT(User leavingUser, Course courseToLeave);

}
