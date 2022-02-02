package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.*;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopics;
import com.henrique.virtualteacher.models.LectureModel;
import com.henrique.virtualteacher.models.Status;
import com.henrique.virtualteacher.services.interfaces.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/courses")
public class CourseRestController {

private final CourseService courseService;
private final UserService userService;
private final LectureService lectureService;
private final RatingService ratingService;
private final AssignmentService assignmentService;
private final Logger logger;
private final ModelMapper mapper;


    private List<CourseModel> mapAllToModel(List<Course> courses, User loggedUser, boolean includeCompletionPercentage) {
        List<CourseModel> dtoList = new ArrayList<>();

        for (Course current : courses) {
            CourseModel courseModel = mapper.map(current, new TypeToken<CourseModel>() {}.getType());
            courseModel.setAverageRating(ratingService.getAverageRatingForCourse(current));

            if (includeCompletionPercentage) {
                courseModel.setCourseCompletionPercentage(courseService.getPercentageOfCompletedCourseLectures(loggedUser, current));
            }
            dtoList.add(courseModel);
        }
        return dtoList;
    }

    @GetMapping("/enrolled")
    public ResponseEntity<Model> enrolledCourses(Principal principal,
                                                        Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        List<CourseModel> courseModels = mapAllToModel(loggedUser.getEnrolledCourses(), loggedUser, true);

        model.addAttribute("enrolledCourses", courseModels);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/completed")
    public ResponseEntity<Model> completedCourses(Principal principal,
                                                  Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        List<CourseModel> courseModels = mapAllToModel(loggedUser.getCompletedCourses(), loggedUser, true);
        model.addAttribute("completedCourses", courseModels);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> getById(@PathVariable int id,
                                         Model model) {

        Course course = courseService.getById(id);
        CourseModel courseModel = new CourseModel();
        double averageRating = ratingService.getAverageRatingForCourse(course);

        mapper.map(course, courseModel);

        model.addAttribute("course", course);
        model.addAttribute("courseAverageRating", averageRating);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/enabled")
    public ResponseEntity<Model> getAllEnabled(Principal principal,
                                               Model model) {
        User loggedUser = userService.getByEmail(principal.getName());

        List<Course> enabledCourses = courseService.getByEnabled(true);
        model.addAttribute("enabledCourses", enabledCourses);

        return new ResponseEntity<>(model,HttpStatus.ACCEPTED);
    }

    @GetMapping("/disabled")
    ResponseEntity<Model> getAllDisabled(Principal principal,
                                                     Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        List<Course> disabledCourses = courseService.getByEnabled(false);
        model.addAttribute("disabledCourses", disabledCourses);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/all")
    public ResponseEntity<Model> getAll(Principal principal,
                                        Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        List<Course> courses = courseService.getAll();
        List<CourseModel> dtoList = mapAllToModel(courses, loggedUser, false);

        model.addAttribute("allCourses", dtoList);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Model> activateCourse(@PathVariable int id,
                                                     Principal principal,
                                                     Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);

        courseService.enableCourse(course, loggedUser);
        logger.info(String.format("Course with id: {%d}, was enabled",course.getId()));

        model.addAttribute("GOOGLE HACKER", course.isEnabled());
        return new ResponseEntity<>(model , HttpStatus.OK);
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Model> disableCourse(@PathVariable int id,
                                                    Principal principal,
                                                    Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);

        courseService.disableCourse(course, loggedUser);

        logger.info(String.format("Course with id: {%d}, was disabled",course.getId()));
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Boolean> create(@RequestBody CourseModel courseModel,
                                          Authentication authentication) {

        User loggedUser = userService.getByEmail(authentication.getName());
        courseService.create(courseModel, loggedUser);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Model> update(@PathVariable int id,
                                        @RequestBody CourseModel courseModel,
                                        Principal principal,
                                        Model model) throws ParseException {

        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);

        courseService.update(courseModel, course, loggedUser);

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable int id,
                       Principal principal) {

    //fixme: delete causes error, in the foreign key table containing lectures

        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);

        lectureService.deleteAllByCourseId(course.getId(), loggedUser);
        courseService.delete(course, loggedUser);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{id}/lecture/{entryId}")
    public ResponseEntity<Model> getCourseLecture(@PathVariable int id,
                                                         @PathVariable int entryId,
                                                         Principal principal,
                                                         Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        Lecture lecture = lectureService.getByEntryIdAndCourseId(entryId, id);
        LectureModel lectureModel = mapper.map(lecture, new TypeToken<LectureModel>() {}.getType());

        model.addAttribute("courseLecture", lectureModel);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/{id}/lecture/{entryId}/submit")
    public ResponseEntity<Model> submitAssignment(@PathVariable("id") int courseId,
                                                  @PathVariable int entryId,
                                                  @RequestParam String content,
                                                  Principal principal,
                                                  Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(courseId);
        Lecture lecture = lectureService.getByEntryIdAndCourseId(entryId, courseId);

        Assignment assignment = new Assignment();
        assignment.setUser(loggedUser);
        assignment.setLecture(lecture);
        assignment.setContent(content);
        assignment.setStatus(Status.PENDING);

        assignmentService.create(assignment, loggedUser);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{id}/lecture/{entryId}")
    public ResponseEntity<Boolean> completeCourseLecture(@PathVariable int id,
                                                         @PathVariable int entryId,
                                                         Principal principal) {


        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);
        Lecture lecture = lectureService.getByEntryIdAndCourseId(entryId, id);

        courseService.verifyUserIsEnrolledToCourse(loggedUser, course);
        //Todo: check if user has submitted assignment before completing the lecture
        // dont do now as this makes testing way slower
        lectureService.completeLectureForUser(loggedUser, lecture);

        logger.info(String.format("User with id: %d has completed the lecture with entryId: %d for the Course with id %d", loggedUser.getId(), lecture.getEntryId(), course.getId()));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    // we create a lecture and add it to the course
    @PostMapping("/{id}")
    public ResponseEntity<Boolean> addLectureToCourse(@PathVariable int id,
                                                @RequestBody LectureModel lectureModel,
                                                Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());

        if (loggedUser.isNotTeacherOrAdmin()) {
            throw new UnauthorizedOperationException("User", "id", String.valueOf(loggedUser.getId()), "add lecture", "Course", "id", String.valueOf(id));
        }

        Course course = courseService.getById(id);
        Lecture lecture = lectureService.mapModelToEntity(lectureModel, course);

        lectureService.create(lecture, loggedUser);
        lecture = lectureService.getByTitle(lecture.getTitle());
        courseService.addLectureToCourse(lecture, id);

        logger.info(String.format("Lecture with id: %d, has been added to the course with id: %d", lecture.getId(), course.getId()));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @PostMapping("/{id}/enroll")
    public ResponseEntity<Boolean> enroll(@PathVariable int id,
                                          Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Course course  = courseService.getById(id);

        courseService.enroll(course, loggedUser);

        logger.info(String.format("User with id: %d, has enrolled into Course with id: %d", loggedUser.getId(), course.getId()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<HttpStatus> complete(@PathVariable int id,
                                               Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Course completedCourse = courseService.getById(id);

        courseService.complete(completedCourse, loggedUser);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Model> rate(@PathVariable int id,
                                      @RequestParam int rating,
                                      Principal principal,
                                      Model model) {


        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);
        ratingService.create(course, loggedUser, rating);

        model.addAttribute("averageRating", ratingService.getAverageRatingForCourse(course));

        return new ResponseEntity<>(model ,HttpStatus.OK);
    }

}
