package com.henrique.virtualteacher.services.implementation;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.EntityNotFoundException;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopics;
import com.henrique.virtualteacher.repositories.CourseRepository;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String ALREADY_ENROLLED_MSG = "User with id: %d is already enrolled in course with id: %d";

    private final CourseRepository courseRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Override
    public boolean create(CourseModel course) {

        //todo: check for invalid information will be done in js

        Course newCourse = new Course();
        mapCourse(course, newCourse);

        try {
            courseRepository.save(newCourse);
        } catch (IllegalArgumentException e){
            return false;
        }
        return true;
    }

    private void mapCourse(CourseModel dto, Course newCourse) {
        mapper.map(dto, newCourse);
        newCourse.setEnabled(true);
    }

    @Override
    public void update(Course course) {

    }

    @Override
    public void delete(Course course) {

    }

    public void enroll(Principal principal, int courseId){

        User loggedUser = userService.getByEmail(principal.getName());
        Course course = getById(courseId, loggedUser);

        if (!loggedUser.enrollToCourse(course)) {
            throw new ImpossibleOperationException(String.format(ALREADY_ENROLLED_MSG, loggedUser.getId(), courseId));
        }

        userRepository.save(loggedUser);
    }

    @Override
    public Course getById(int id, User loggedUser) {


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
