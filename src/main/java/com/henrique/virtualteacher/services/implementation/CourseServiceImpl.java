package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.configurations.CloudinaryConfig;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumDifficulty;
import com.henrique.virtualteacher.models.EnumTopic;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.LectureService;
import com.henrique.virtualteacher.services.interfaces.RatingService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;

import org.modelmapper.TypeToken;
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
    private final ModelMapper mapper;
    private final Logger logger;
    private final LectureService lectureService;
    private final CloudinaryConfig cloudinaryConfig;
    private final RatingService ratingService;

    @Override
    public List<CourseModel> mapAllToModel(List<Course> courses, User loggedUser, boolean includeCompletionAmount) {
        List<CourseModel> dtoList = new ArrayList<>();

        for (Course current : courses) {
            CourseModel courseModel = mapper.map(current, new TypeToken<CourseModel>() {}.getType());
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
    public void create(CourseModel course, User loggedUser) {

        //todo: check for invalid information will be done in js

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(USER_UNAUTHORIZED_ERROR_MSG);
        }
        try {
            getByTitle(course.getTitle());
            throw new DuplicateEntityException("Course", "Title", course.getTitle());

        } catch (EntityNotFoundException e) {

            Course newCourse = new Course();
            mapCourse(course, newCourse);

            courseRepository.save(newCourse);
        }
    }

    private void mapCourse(CourseModel dto, Course newCourse) {
        mapper.map(dto, newCourse);
        newCourse.setEnabled(false);
    }

    @Override
    public void update(CourseModel courseModel, Course courseToUpdate, User loggedUser) throws ParseException {

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("User", "username", loggedUser.getEmail(), "update", "Course", "title", courseToUpdate.getTitle());
        }
        if (titleAlreadyExists(courseModel.getTitle(), courseToUpdate.getTitle())) {
            throw new DuplicateEntityException("Course", "title", courseModel.getTitle());
        }

        mapper.map(courseModel, courseToUpdate);

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
        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("User", "username", loggedUser.getEmail(), "delete", "Course", "title", course.getTitle());
        }
        course.getEnrolledUsers().clear();
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
    public void enroll(Course course, User loggedUser){
        loggedUser.enrollToCourse(course);

        logger.info(String.format("User with id: {%d}, has enrolled into course with id: {%d}", loggedUser.getId(), course.getId()));
        userRepository.save(loggedUser);
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
        if (loggedUser.isNotTeacherOrAdmin()) {
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
        return mapAllToModel(topThree, null, false);
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

        if (!loggedUser.isEnrolledInCourse(course)) {
            throw new ImpossibleOperationException(String.format("User with id: %d, is not enrolled into Course with id %d", loggedUser.getId(), course.getId()));
        }

    }

    public void addLectureToCourse(Lecture lecture, int courseId) {
        Course course = getById(courseId);
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
        return mapAllToModel(courseRepository.findByTopic(topic), null,false);
    }

    @Override
    public List<CourseModel> getAllByEnabled(boolean isEnabled,  Optional<User> loggedUser) {

        List<CourseModel> courseModels;
        courseModels = mapAllToModel(courseRepository.findByEnabled(isEnabled)
                .stream()
                .limit(20).collect(Collectors.toList()), null, false);

        return courseModels;
    }

    @Override
    public List<Course> getAllByDifficulty(EnumDifficulty difficultyLevel) {
        return courseRepository.findByDifficulty(difficultyLevel);
    }
}
