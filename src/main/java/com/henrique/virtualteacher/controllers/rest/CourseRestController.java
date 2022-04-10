package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.configurations.CloudinaryConfig;
import com.henrique.virtualteacher.entities.Assignment;
import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Lecture;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.UnauthorizedOperationException;
import com.henrique.virtualteacher.models.*;
import com.henrique.virtualteacher.services.interfaces.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/courses")
public class CourseRestController {

private final CourseService courseService;
private final UserService userService;
private final LectureService lectureService;
private final AssignmentService assignmentService;
private final RatingService ratingService;
private final CommentService commentService;
private final CloudinaryConfig cloudinaryConfig;
private final Logger logger;
private final ModelMapper mapper;




    @GetMapping("/enrolled")
    public ResponseEntity<Model> enrolledCourses(Principal principal,
                                                        Model model) {

        User loggedUser = userService.getByEmail(principal.getName());
        List<CourseModel> courseModels = courseService.mapAllToModel(loggedUser.getEnrolledCourses(), loggedUser, true);

        model.addAttribute("enrolledCourses", courseModels);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/completed")
    public ResponseEntity<Model> completedCourses(Principal principal,
                                                  Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        List<CourseModel> courseModels = courseService.mapAllToModel(loggedUser.getCompletedCourses(), loggedUser, true);
        model.addAttribute("completedCourses", courseModels);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModelAndView> getById(@PathVariable int id,
                                         ModelAndView model) {

        Course course = courseService.getById(id);
        CourseModel courseModel = new CourseModel();
//        double averageRating = ratingService.getAverageRatingForCourse(course);

        mapper.map(course, courseModel);

        model.addObject("course", course);
//        model.addObject("courseAverageRating", averageRating);
        model.addObject("courseComments", commentService.getAllForCourse(id));

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<CourseModel>> getAllEnabled(Principal principal,
                                               Model model) {

        Optional<User> loggedUser;

        if (principal == null) {
            loggedUser = Optional.empty();
        } else {
            loggedUser = Optional.of(userService.getByEmail(principal.getName()));
        }

        List<CourseModel> enabledCourses = courseService.getAllByEnabled(true, loggedUser);

        return new ResponseEntity<>(enabledCourses,HttpStatus.ACCEPTED);
    }

    @GetMapping("/disabled")
    ResponseEntity<Model> getAllDisabled(Principal principal,
                                                     Model model) {


        User loggedUser = userService.getByEmail(principal.getName());

        List<CourseModel> disabledCourses = courseService.getAllByEnabled(false, Optional.of(loggedUser));
        model.addAttribute("disabledCourses", disabledCourses);

        return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
    }

    @GetMapping("/all")
    public ResponseEntity<Model> getAll(Principal principal,
                                        Model model) {

        User loggedUser = userService.getByEmail(principal.getName());

        List<Course> courses = courseService.getAll();
        List<CourseModel> dtoList = courseService.mapAllToModel(courses, loggedUser, false);

        model.addAttribute("allCourses", dtoList);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Set<CommentModel>> getCourseComments(@PathVariable int id) {
        return new ResponseEntity<>(commentService.getAllForCourse(id), HttpStatus.ACCEPTED);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Boolean> createCourseComment(@PathVariable int id,
                                                       @RequestParam String comment,
                                                       Principal principal) {
        User loggedUser = userService.getByEmail(principal.getName());
        Course course = courseService.getById(id);

        if (comment == null || comment.trim().equals("")) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "comment is blank");
        }
        commentService.create(loggedUser, course, comment);
        return new ResponseEntity<>(true, HttpStatus.CREATED);
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

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> create(@RequestBody CourseModel courseModel,
                                          Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        courseService.create(courseModel, loggedUser);

        return new ResponseEntity<>("success", HttpStatus.OK);
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

        assignmentService.create(assignment);

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


    @PostMapping("/{id}/purchase")
    public ResponseEntity<Boolean> enroll(@PathVariable int id,
                                          Principal principal) {

        User loggedUser = userService.getByEmail(principal.getName());
        Course course  = courseService.getById(id);

        courseService.purchase(loggedUser, course);

        logger.info(String.format("User with id: %d, has purchased Course with id: %d", loggedUser.getId(), course.getId()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @PostMapping("/enroll")
//    public ResponseEntity<Boolean> enroll(@RequestParam String courseTitle,
//                                          Principal principal) {
//
//        User loggedUser = userService.getByEmail(principal.getName());
//        Course course  = courseService.getByTitle(courseTitle);
//
//        try {
//            courseService.enroll(course, loggedUser);
//        } catch (ImpossibleOperationException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//
//        logger.info(String.format("User with id: %d, has enrolled into Course with id: %d", loggedUser.getId(), course.getId()));
//        return new ResponseEntity<>(true, HttpStatus.OK);
//    }

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

        return new ResponseEntity<>(model ,HttpStatus.OK);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<CourseModel> changePicture(@PathVariable int id,
                                                Principal principal,
                                                MultipartFile multipartFile) throws IOException {

        User loggedUser = userService.getByEmail(principal.getName());

        courseService.upload(multipartFile, id, loggedUser);

        CourseModel courseModel = mapper.map(courseService.getById(id), new TypeToken<CourseModel>() {}.getType());
        return new ResponseEntity<>(courseModel, HttpStatus.ACCEPTED);
    }

    @GetMapping("/topics")
    public ResponseEntity<List<EnumTopic>> getCourseTopics() {

        List<EnumTopic> enumTopicSet = Arrays.stream(EnumTopic.values()).collect(Collectors.toList());
        return new ResponseEntity<>(enumTopicSet, HttpStatus.ACCEPTED);
    }

}
