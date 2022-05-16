package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.NFTCourse;
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
    public NFTCourse getById(int id) {
        return nftCourseRepository.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course Enrollment", "ID", String.valueOf(id)));
    }

    @Override
    public List<NFTCourse> getAllForUser(User loggedUser, int userToGetId) {
        checkUserIsAllowed(loggedUser, userToGetId);
        return nftCourseRepository.getAllByOwnerId(userToGetId);
    }

    private void checkUserIsAllowed(User loggedUser, int userToGetId) {
        if (loggedUser.getId() != userToGetId && loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, does nto have access to the courses of user with id: %d", loggedUser.getId(), userToGetId));
        }
    }

    @Override
    public List<NFTCourse> getAllForCourse(User loggedUser, int courseToGetId) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id %d, does not have access to the users enrolled to course with id: %d", loggedUser.getId(), courseToGetId));
        }
        return nftCourseRepository.getAllByCourseId(courseToGetId);
    }

    @Override
    public List<NFTCourse> getAllForUser(User loggedUser, int userToGetId, boolean completed) {
        checkUserIsAllowed(loggedUser, userToGetId);
        return nftCourseRepository.getAllByOwnerIdAndCompleted(userToGetId, completed);
    }

    @Override
    public List<NFTCourse> getAllForCourse(User loggedUser, int courseToGetId, boolean completed) {
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id %d, does not have access to the users enrolled to course with id: %d", loggedUser.getId(), courseToGetId));
        }
        return nftCourseRepository.getAllByCourseIdAndCompleted(courseToGetId, completed);
    }

    @Override
    public List<NFTCourse> getAllForCourseByMinted(User loggedUser, int courseToGet, boolean minted) {
        return nftCourseRepository.getAllNonMintedFromCourse(courseToGet, minted);
    }

    @Override
    public NFTCourse getUserOwnedNFTCourse(User loggedUser, Course enrolledCourse) {
        return nftCourseRepository.getByOwnerIdAndCourseId(loggedUser.getId(), enrolledCourse.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id: %d, is not enrolled to course with id: %d", loggedUser.getId(), enrolledCourse.getId())));
    }

    @Override
    public NFTCourse purchase(User purchaser, Course courseToPurchaseNft) {
        if (purchaser.hasPurchasedCourse(courseToPurchaseNft)){
            throw new ImpossibleOperationException(String.format("User with id: {%d}, has already purchased course with id: {%d}", purchaser.getId(), courseToPurchaseNft.getId()));
        }

        List<NFTCourse> availableNFTs = nftCourseRepository.getAllByCourseIdAndMinted(courseToPurchaseNft.getId(), false);
        if (availableNFTs.isEmpty()) {
            throw new ImpossibleOperationException(String.format("There are no available mints for the course with id %d",courseToPurchaseNft.getId()));
        }

        NFTCourse newNFTCourse = availableNFTs.get(0);
        changeNftOwnership(purchaser, newNFTCourse);

        NFTCourse created = nftCourseRepository.save(newNFTCourse);
        logger.info(String.format("User with id: %d, has purchased course NFT with drop id %d of course with id %d", purchaser.getId(), newNFTCourse.getDropNumber(), newNFTCourse.getCourse().getId()));
        return created;
    }

    public void checkCourseHasAvailableMints(User loggedUser, int courseId) {
        List<NFTCourse> availableNFTs = nftCourseRepository.getAllByCourseIdAndMinted(courseId, false);
        if (availableNFTs.isEmpty()) {
            throw new ImpossibleOperationException(String.format("There are no available mints for the course with id %d", courseId));
        }
    }

    private void changeNftOwnership(User newOwner, NFTCourse nftCourse) {
        nftCourse.setOwner(newOwner);
        nftCourse.setMinted(true);
    }

    @Override
    public void createCourseNFTItems(Course course, User loggedUser) {
        checkUserIsAllowed(loggedUser, course.getCreator().getId());

        for(int index = 0; index < course.getAvailableMints(); index++) {
            NFTCourse nftCourse = new NFTCourse(course, index);
            nftCourseRepository.save(nftCourse);
        }
    }

    @Override
    public void leave(User leavingUser, Course courseToLeave) {
        if (!leavingUser.hasPurchasedCourse(courseToLeave)){
            throw new ImpossibleOperationException(String.format("User with id: %d cannot leave course with id: %d, as the user is not enrolled",leavingUser.getId(), courseToLeave.getId()));
        }
        NFTCourse toDelete = getUserOwnedNFTCourse(leavingUser, courseToLeave);
        nftCourseRepository.delete(toDelete);
    }

}
