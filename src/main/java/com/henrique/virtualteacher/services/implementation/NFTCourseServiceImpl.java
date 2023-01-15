package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.NFT;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.repositories.NFTCourseRepository;
import com.henrique.virtualteacher.services.interfaces.NFTCourseService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NFTCourseServiceImpl implements NFTCourseService {

    private final NFTCourseRepository nftCourseRepository;
    private final Logger logger;

    @Override
    public NFT getById(int id) {
        return nftCourseRepository.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course Enrollment", "ID", String.valueOf(id)));
    }


    @Override
    public List<NFT> getAllForUser(User loggedUser, int userToGetId) {
        checkUserIsAllowed(loggedUser, userToGetId);
        return nftCourseRepository.getAllByOwnerId(userToGetId);
    }

    private void checkUserIsAllowed(User loggedUser, int userToGetId) {
        if (loggedUser.getId() != userToGetId && loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, does nto have access to the courses of user with id: %d", loggedUser.getId(), userToGetId));
        }
    }

    @Override
    public List<NFT> getAllForCourse(User loggedUser, int courseToGetId) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id %d, does not have access to the users enrolled to course with id: %d", loggedUser.getId(), courseToGetId));
        }
        return nftCourseRepository.getAllByCourseId(courseToGetId);
    }

    @Override
    public List<NFT> getAllForUser(User loggedUser, int userToGetId, boolean completed) {
        checkUserIsAllowed(loggedUser, userToGetId);
        return nftCourseRepository.getAllByOwnerIdAndCompleted(userToGetId, completed);
    }

    @Override
    public List<NFT> getAllForCourse(User loggedUser, int courseToGetId, boolean completed) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id %d, does not have access to the users enrolled to course with id: %d", loggedUser.getId(), courseToGetId));
        }
        return nftCourseRepository.getAllByCourseIdAndCompleted(courseToGetId, completed);
    }

    @Override
    public NFT getUserOwnedNFTCourse(User loggedUser, Course enrolledCourse) {
        return nftCourseRepository.getByOwnerIdAndCourseId(loggedUser.getId(), enrolledCourse.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id: %d, is not enrolled to course with id: %d", loggedUser.getId(), enrolledCourse.getId())));
    }

//    @Override
//    public NFT purchase(User purchaser, Course courseToPurchaseNft) {
//
//        List<NFT> availableNFTs = nftCourseRepository.getAllByCourseIdAndMinted(courseToPurchaseNft.getId(), false);
//        if (availableNFTs.isEmpty()) {
//            throw new ImpossibleOperationException(String.format("There are no available mints for the course with id %d",courseToPurchaseNft.getId()));
//        }
//
//        NFT newNFT = availableNFTs.get(0);
//        changeNftOwnership(purchaser, newNFT);
//
//        NFT created = nftCourseRepository.save(newNFT);
//        logger.info(String.format("User with id: %d, has MINTED course NFT with drop id %d of course with id %d", purchaser.getId(), newNFT.getDropNumber(), newNFT.getCourse().getId()));
//        return created;
//    }

    public NFT mintNFT(User purchaser, Course courseToPurchaseNft) {

        checkCourseHasAvailableMints(courseToPurchaseNft);
        NFT newMint = new NFT(purchaser, courseToPurchaseNft);
        return nftCourseRepository.save(newMint);
    }

    public void checkCourseHasAvailableMints(Course course) {
        if (course.getNfts().size() >= course.getAvailableMints()) {
            throw new ImpossibleOperationException(String.format("Course with id: %d, does not have available mints", course.getId()));
        }
    }


    private void changeNftOwnership(User newOwner, NFT nft) {
        nft.setOwner(newOwner);
    }

    @Override
    public void burnNFT(User leavingUser, Course courseToLeave) {
        if (!leavingUser.hasPurchasedCourse(courseToLeave)){
            throw new ImpossibleOperationException(String.format("User with id: %d cannot leave course with id: %d, as the user is not enrolled",leavingUser.getId(), courseToLeave.getId()));
        }
        NFT toDelete = getUserOwnedNFTCourse(leavingUser, courseToLeave);
        nftCourseRepository.delete(toDelete);
    }

}
