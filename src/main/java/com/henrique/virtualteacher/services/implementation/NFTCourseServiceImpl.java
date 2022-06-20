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
    public List<NFT> getAllForCourseByMinted(User loggedUser, int courseToGet, boolean minted) {
        return nftCourseRepository.getAllNonMintedFromCourse(courseToGet, minted);
    }

    @Override
    public NFT getUserOwnedNFTCourse(User loggedUser, Course enrolledCourse) {
        return nftCourseRepository.getByOwnerIdAndCourseId(loggedUser.getId(), enrolledCourse.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id: %d, is not enrolled to course with id: %d", loggedUser.getId(), enrolledCourse.getId())));
    }

    @Override
    public NFT purchase(User purchaser, Course courseToPurchaseNft) {
        if (purchaser.hasPurchasedCourse(courseToPurchaseNft)){
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already purchased course with id: {%d}", purchaser.getId(), courseToPurchaseNft.getId()));
        }

        List<NFT> availableNFTs = nftCourseRepository.getAllByCourseIdAndMinted(courseToPurchaseNft.getId(), false);
        if (availableNFTs.isEmpty()) {
            throw new ImpossibleOperationException(String.format("There are no available mints for the course with id %d",courseToPurchaseNft.getId()));
        }

        NFT newNFT = availableNFTs.get(0);
        changeNftOwnership(purchaser, newNFT);

        NFT created = nftCourseRepository.save(newNFT);
        logger.info(String.format("User with id: %d, has purchased course NFT with drop id %d of course with id %d", purchaser.getId(), newNFT.getDropNumber(), newNFT.getCourse().getId()));
        return created;
    }

    public void checkCourseHasAvailableMints(User loggedUser, int courseId) {
        List<NFT> availableNFTs = nftCourseRepository.getAllByCourseIdAndMinted(courseId, false);
        if (availableNFTs.isEmpty()) {
            throw new ImpossibleOperationException(String.format("There are no available mints for the course with id %d", courseId));
        }
    }

    private void changeNftOwnership(User newOwner, NFT nft) {
        nft.setOwner(newOwner);
        nft.setMinted(true);
    }

    @Override
    public void createCourseNFTItems(Course course, User loggedUser) {
        checkUserIsAllowed(loggedUser, course.getCreator().getId());

        for(int index = 0; index < course.getAvailableMints(); index++) {
            NFT nft = new NFT(course, index);
            nftCourseRepository.save(nft);
        }
    }

    @Override
    public void leave(User leavingUser, Course courseToLeave) {
        if (!leavingUser.hasPurchasedCourse(courseToLeave)){
            throw new ImpossibleOperationException(String.format("User with id: %d cannot leave course with id: %d, as the user is not enrolled",leavingUser.getId(), courseToLeave.getId()));
        }
        NFT toDelete = getUserOwnedNFTCourse(leavingUser, courseToLeave);
        nftCourseRepository.delete(toDelete);
    }

}
