package com.henrique.virtualteacher.controllers.rest;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.exceptions.ImpossibleOperationException;
import com.henrique.virtualteacher.models.CourseModel;
import com.henrique.virtualteacher.models.EnumTopics;
import com.henrique.virtualteacher.services.interfaces.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseRestController {

private final CourseService courseService;

    @GetMapping()
    public ResponseEntity<List<Course>> getAll(){
        return new ResponseEntity<>(courseService.getAll(), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Boolean> create(@RequestBody CourseModel courseModel) {

        HttpStatus status;
        boolean courseWasCreated = courseService.create(courseModel);

        if (courseWasCreated) {
            status = HttpStatus.OK;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(status);
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<Boolean> enroll(@PathVariable int id,
                                          Principal principal) {

        HttpStatus status;
        try {
            courseService.enroll(principal, id);
            status = HttpStatus.ACCEPTED;
        } catch (ImpossibleOperationException e) {
            status = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(status);
    }

}
