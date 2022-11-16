package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.configurations.CloudinaryConfig;
import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumDifficulty;
import com.henrique.virtualteacher.models.EnumTopic;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String ALREADY_ENROLLED_MSG = "User with id: %d is already enrolled in course with id: %d";
    private static final String USER_UNAUTHORIZED_ERROR_MSG = "You are not authorized to perform this operation";

    private final CourseRepository courseRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final Logger logger;
    private final LectureService lectureService;
    private final CloudinaryConfig cloudinaryConfig;
    private final RatingService ratingService;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final NFTCourseService nftCourseService;

    @Override
    public List<CourseModel> mapAllToModel(List<Course> courses, User loggedUser, boolean includeCompletionAmount) {
        List<CourseModel> dtoList = new ArrayList<>();

        for (Course current : courses) {
            CourseModel courseModel = mapCourseModel(current);
            courseModel.setAverageRating(Math.round(ratingService.getAverageRatingForCourse(current) * 100.0) / 100.0);

            if (loggedUser != null) {
                if (includeCompletionAmount) {
                    courseModel.setCourseCompletionPercentage(getPercentageOfCompletedCourseLectures(loggedUser, current));
                }
            }
            dtoList.add(courseModel);
        }
        return dtoList;
    }

    @Override
    public List<CourseModel> mapAllToModel(List<Course> courses) {
        List<CourseModel> dtoList = new ArrayList<>();

        for (Course current : courses) {
            CourseModel courseModel = mapCourseModel(current);
            courseModel.setAverageRating(Math.round(ratingService.getAverageRatingForCourse(current) * 100.0) / 100.0);
            dtoList.add(courseModel);
        }
        return dtoList;
    }

    @Override
    public void create(CourseModel course, User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(USER_UNAUTHORIZED_ERROR_MSG);
        }
        try {
            getByTitle(course.getTitle());
            throw new DuplicateEntityException("Course", "Title", course.getTitle());

        } catch (EntityNotFoundException e) {

            Course newCourse = mapCourse(course);
            newCourse = courseRepository.save(newCourse);
            nftCourseService.createCourseNFTItems(newCourse, loggedUser);
        }
    }

    private Course mapCourse(CourseModel dto, int id) {
        Course mappedCourse = mapCourse(dto);
        mappedCourse.setId(id);
        return mappedCourse;
    }

    private Course mapCourse(CourseModel dto) {
        Course course = new Course();
        course.setTopic(dto.getTopic());
        course.setTitle(dto.getTitle());
        User creator = userService.getByEmail(dto.getCreatorEmail());
        course.setCreator(creator);
        course.setPrice(dto.getPrice());
        course.setDescription(dto.getDescription());
        course.setDifficulty(dto.getDifficulty());
        course.setPicture(dto.getPicture());
        course.setStartingDate(dto.getStartingDate());
        course.setSkill1(dto.getSkill1());
        course.setSkill2(dto.getSkill2());
        course.setSkill3(dto.getSkill3());
        course.setEnabled(false);
        return course;
    }

    private void mapToCourse(CourseModel courseModel, Course courseToUpdate) {

    }

    private CourseModel mapCourseModel(Course course) {
        CourseModel courseModel = new CourseModel();
        courseModel.setId(course.getId());
        courseModel.setTitle(course.getTitle());
        courseModel.setCreatorEmail(course.getCreator().getEmail());
        courseModel.setPrice(course.getPrice());
        courseModel.setTopic(course.getTopic());
        courseModel.setDifficulty(course.getDifficulty());
        courseModel.setPicture(course.getPicture());
        courseModel.setStartingDate(course.getStartingDate());
        courseModel.setSkill1(course.getSkill1());
        courseModel.setSkill2(course.getSkill2());
        courseModel.setSkill3(course.getSkill3());
        courseModel.setDescription(course.getDescription());
        return courseModel;
    }

    @Override
    public void update(CourseModel courseModel, Course courseToUpdate, User loggedUser) throws ParseException {

        if (loggedUser.getId() != courseToUpdate.getCreator().getId() && loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("User", "username", loggedUser.getEmail(), "update", "Course", "title", courseToUpdate.getTitle());
        }
        if (titleAlreadyExists(courseModel.getTitle(), courseToUpdate.getTitle())) {
            throw new DuplicateEntityException("Course", "title", courseModel.getTitle());
        }

        courseToUpdate = mapCourse(courseModel, courseToUpdate.getId());
        courseRepository.save(courseToUpdate);
    }

    public boolean titleAlreadyExists(String title, String currentTitle) {

        if (currentTitle.equals(title)) {
            return false;
        }

        try {
            getByTitle(title);
        } catch (EntityNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void delete(Course course, User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin() && loggedUser.getId() != course.getCreator().getId()) {
            throw new UnauthorizedOperationException("User", "username", loggedUser.getEmail(), "delete", "Course", "title", course.getTitle());
        }
        course.getNfts().clear();
        course.getRatings().clear();
        courseRepository.delete(course);
    }

    public void enableCourse(Course course, User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("");
        }
        if (course.isEnabled()) {
            throw new ImpossibleOperationException(String.format("Course with id: {%d} is already enabled", course.getId()));
        }

        course.setEnabled(true);
        courseRepository.save(course);
    }

    public void disableCourse(Course course, User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("");
        }

        if (!course.isEnabled()) {
            throw new ImpossibleOperationException(String.format("Course with id: {%d} is already disabled", course.getId()));
        }

        course.setEnabled(false);
        courseRepository.save(course);
    }

    @Override
    public void mint(User loggedUser, Course courseToPurchase) {

        if (loggedUser.hasPurchasedCourse(courseToPurchase)) {
            throw new DuplicateEntityException(String.format("User with id: %d, is already enrolled to course with id: %d", loggedUser.getId(), courseToPurchase.getId()));
            //todo : add max mint per person field in courses, and check if user has minted the max amount.
        }

        nftCourseService.checkCourseHasAvailableMints(loggedUser, courseToPurchase.getId());
        NFT mintedCourse = walletService.mintNFT(courseToPurchase, loggedUser);
        createTransaction(loggedUser, mintedCourse);
    }

    private void createTransaction(User loggedUser, NFT nft) {
        Wallet senderWallet = walletService.getLoggedUserWallet(loggedUser);
        Wallet recipientWallet = walletService.getLoggedUserWallet(nft.getOwner());
        Transaction transaction = new Transaction(senderWallet, recipientWallet, nft);

        nftCourseService.purchase(loggedUser, nft.getCourse());
        transactionService.create(transaction, loggedUser);
    }

    @Override
    public void complete(Course course, User loggedUser) {

        verifyUserIsEnrolledToCourse(loggedUser, course);
        verifyUserHasCompletedAllCourseLectures(loggedUser, course);

        loggedUser.completeCourse(course);

        logger.info(String.format("User with id: {%d}, has completed the course with id: {%d}", loggedUser.getId(), course.getId()));
        userRepository.save(loggedUser);
    }

    @Override
    public void upload(MultipartFile file, int courseId, User loggedUser) throws IOException {

        Course course = getById(courseId);
        if (loggedUser.getId() != course.getCreator().getId() && !loggedUser.isAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: {%d}, is not authorized to change Course information",loggedUser.getId()));
        }

        String uploadedFileUrl = cloudinaryConfig.upload(file);
        course.setPicture(uploadedFileUrl);
        courseRepository.save(course);
    }

    public int getPercentageOfCompletedCourseLectures(User loggedUser, Course course) {

        int completedCourseLectures = (int) loggedUser.getCompletedLectures()
                .stream()
                .filter(lecture -> lecture.getCourse().getId() == course.getId())
                .count();

        int totalCourseLectures = course.getCourseLectures().size();

        int percentage = (int) Math.round(completedCourseLectures * 100.0 / totalCourseLectures);
        return percentage;
    }

    public List<CourseModel> getTopTheeCoursesByRating() {
        List<Course> topThree = courseRepository.getThreeRandomCourses();
        return mapAllToModel(topThree);
    }

    @Override
    public CourseModel mapToModel(Course course) {
        return new CourseModel(course);
    }

    @Override
    public void verifyUserHasCompletedAllCourseLectures(User user, Course course) {

        verifyUserIsEnrolledToCourse(user, course);

        List<Lecture> courseLectures = course.getCourseLectures();

        List<Lecture> userCompletedCourseLectures =  user.getCompletedLectures().stream()
                .filter(lecture -> lecture.getCourse().getId() == course.getId())
                .collect(Collectors.toList());

        if (userCompletedCourseLectures.size() < courseLectures.size()) {
            throw new ImpossibleOperationException(String.format("User with id: %d has not completed all the lectures of Course with id: %d", user.getId(), course.getId()));
        }
    }

    @Override
    public void verifyUserIsEnrolledToCourse(User loggedUser, Course course) {

        if (!loggedUser.hasPurchasedCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id: %d, is not enrolled into Course with id %d", loggedUser.getId(), course.getId()));
        }
    }

    @Override
    public void addLectureToCourse(Lecture lecture, Course course, User loggedUser) {
        if (loggedUser.getId() != course.getCreator().getId() && !loggedUser.isAdmin()) {
            throw new UnauthorizedOperationException(String.format("User with id: %d, is not authorized to add lectures to course with id: %d", loggedUser.getId(), course.getId()));
        }
        course.addLecture(lecture);
    }

    @Override
    public Course getById(int id) {

       return courseRepository.findById(id)
               .orElseThrow(() -> new EntityNotFoundException("course", "id", String.valueOf(id)));
    }

    @Override
    public Course getByTitle(String title) {
        return courseRepository.findByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("course", "title", title));
    }

    @Override
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    @Override
    public List<CourseModel> getAllByTopic(EnumTopic topic) {
        return mapAllToModel(courseRepository.findByTopic(topic));
    }

    @Override
    public List<CourseModel> getAllByEnabled(boolean isEnabled,  Optional<User> loggedUser) {

        return mapAllToModel(courseRepository.findByEnabled(isEnabled)
                .stream()
                .limit(20).collect(Collectors.toList()));
    }

    @Override
    public List<Course> getAllByDifficulty(EnumDifficulty difficultyLevel) {
        return courseRepository.findByDifficulty(difficultyLevel);
    }
}
