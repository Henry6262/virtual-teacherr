package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.DuplicateEntityException;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopics;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.LectureService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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


    @Override
    public void create(CourseModel course, User loggedUser) {

        //todo: check for invalid information will be done in js

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException(USER_UNAUTHORIZED_ERROR_MSG);
        }

        Course newCourse = new Course();
        mapCourse(course, newCourse);

        courseRepository.save(newCourse);

    }

    private void mapCourse(CourseModel dto, Course newCourse) {
        mapper.map(dto, newCourse);
        newCourse.setEnabled(true);
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

    private boolean titleAlreadyExists(String title, String currentTitle) {

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
        courseRepository.delete(course);
    }

    public void enableCourse(Course course, User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new EntityNotFoundException("");
        }

        if (course.isEnabled()) {
            throw new ImpossibleOperationException(String.format("Course with id: {%d} is already enabled", course.getId()));
        }

        course.setEnabled(true);
        courseRepository.save(course);
    }

    public void disableCourse(Course course, User loggedUser) {

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new EntityNotFoundException("");
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
    public void verifyUserHasCompletedAllCourseLectures(User user, Course course) {

        verifyUserIsEnrolledToCourse(user, course);

        List<Lecture> courseLectures = lectureService.getAllByCourseId(course.getId());

        List<Lecture> userCompletedCourseLectures =  user.getCompletedLectures().stream()
                .filter(lecture -> lecture.getCourse().getId() == course.getId())
                .collect(Collectors.toList());

        if (userCompletedCourseLectures.size() < courseLectures.size()) {
            throw new ImpossibleOperationException(String.format("User with id: %d has not completed all the lectures of Course with id: %d", user.getId(), course.getId()));
        }

    }

    @Override
    public void verifyUserIsEnrolledToCourse(User loggedUser, Course course) {

        if (loggedUser.getEnrolledCourses()
                .stream()
                .noneMatch(c -> c.getId() == course.getId()))
        {
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
        //bug if there are no courses
    }

    @Override
    public List<Course> getByTopic(EnumTopics topic) {
        return courseRepository.findByTopic(topic)
                .orElseThrow(() -> new EntityNotFoundException("course", "topic", topic.name()));
    }

    @Override
    public List<Course> getByEnabled(boolean isEnabled) {
        return courseRepository.findByEnabled(isEnabled)
                .orElseThrow(() -> new EntityNotFoundException("course", "enabled", String.valueOf(isEnabled)));
    }

    @Override
    public List<Course> getByDifficulty(int difficultyLevel) {
        return courseRepository.findByDifficulty(difficultyLevel)
                .orElseThrow(() -> new EntityNotFoundException("course", "difficulty", String.valueOf(difficultyLevel)));
    }
}
